/*global $, console */

var calcentral = calcentral || {};
calcentral.Widgets = calcentral.Widgets || {};
/**
 * @class tasks
 *
 * @description
 * The 'tasks' widget (i.e. Tasks and Assignments) displays aggregated set of tasks and
 * assignments from Canvas, bCal, or other sources, divided into groups per due-date.
 * Tasks are not editable in this version.
 *
 * @version 0.0.1
 * @param {String} tuid Unique id of the widget
 */
calcentral.Widgets.tasks = function(tuid) {

	'use strict';

	/** VARIABLES. **/

	var $tasksContainer = $('#' + tuid);
	var $tasksList = $('.cc-tasks-list', $tasksContainer);
	var $tasksListTemplate = $('#cc-widget-tasks-template', $tasksContainer);
	var $taskLoopTemplate = $('#cc-taskloop-template', $tasksContainer);

	/**
	 * Given data for task/assignments, adds additional fields to each element,
	 * and outputs new data structure organized into time sections (past/present/future).
	 * Course IDs for assignments are keyed to course IDs from courseData (rather than looping).
	 * @param {object} courseData Course list from canvas
	 * @param {object} assignmentData Assignment list from Canvas
	 * @param {object} gTaskData list from Google
	 */
	var renderTasksAssignments = function(courseData, assignmentData, gTaskData) {
		var data = [];
		courseData = courseData[0] || [];
		assignmentData = assignmentData[0] || [];
		gTaskData = gTaskData[0];

		// To account for differences between tasks emitted from various services, set an "emitter"
		// property for the source. This will help in setting different icons, different date processing, etc.
		// Add real JS date object property for later sorting
		$.each(assignmentData, function(index, value){
			value.emitter = 'canvas-assignments';
			value.real_date = new Date(value.start_at);
		});

		// Merge Google tasks data into the main data object.
		// Modify Google task properties to re-use Canvas assignment properties for compatibility.
		// Add real JS date object property for later sorting.
		if (gTaskData && gTaskData.items) {
			$.each(gTaskData.items, function(index, value){
				// Discard "phantom" Google Tasks
				if (value.title.match('^[a-zA-Z0-9]')) {
					// Google "due" property maps to Canvas "start_at" property
					if (value.due) {
						value.start_at = value.due;
						value.real_date = new Date(value.start_at);
					}
					value.html_url = 'https://mail.google.com/tasks/canvas?pli=1'; // Minimal, but it's the only Tasks web UI available
					value.emitter = 'google-tasks';

					// Append modified entry to main data object
					data.push(value);
				}
			});
		}

		data = data.concat(assignmentData);

		var currentTime = new Date();
		// "Tomorrow" is 1 second after midnight on the next calendar date, NOT 24 hours from now.
		var tomorrow = new Date(currentTime.getFullYear(), currentTime.getMonth(), currentTime.getDate() + 1, 0, 0, 1);

		// Init an empty object into which we'll rewrite the data structure, separating tasks into today, tomorrow, future
		// We do some in-place modification of the data object first, then write the completed objects out to sections of newData.
		var newData = {
			'today': [],
			'tomorrow': [],
			'future': [],
			'unscheduled': []
		};

		// Convert timestamps to friendly dates and set overdue flags.
		$.each(data, function(index, value){
			// For Canvas items, set the html_url
			if (data[index].emitter === 'canvas-assignments') {
				// Grep out this assignment's course ID and URL; set matching course properties for assignments.
				// match() returns an array object, so we need its 0th element before using replace().
				// regex technique is slightly verbose to prevent tripping on port assigment in local instances.
				var courseId = parseInt(data[index].html_url.match(/courses\/\d+/g)[0].replace("courses/",""), 10);
				var courseURL = data[index].html_url.match(/^.*\/assignments/g)[0].replace("/assignments","");
				console.log(courseId, courseURL);
				data[index].sourceUrl = courseURL;
				$.each(courseData, function(i, v){
					if (v.id === courseId) {
						data[index].source = v.course_code;
					}
				});
			}

			// Set the default status to 'inprogress'
			// Google sends us the needsAction status
			if (!data[index].status || data[index].status === 'needsAction') {
				data[index].status = 'inprogress';
			}

			// Set due dates and overdue status for items that are dated.
			var dueDate = new Date(value.start_at);

			if (value.emitter === 'google-tasks') {
				// Google timestamps are UTC = account for timezone offsets to render actual time entered
				var offset = dueDate.getTimezoneOffset();
				var hours = dueDate.getHours();
				dueDate.setHours(hours + offset / 24);
			}

			// Javascript months are 0-based, while days and years are 1-based, so add 1 to month
			var friendlyDate = dueDate.getMonth() + 1 + '/' + dueDate.getDate();
			data[index].friendlyDate = friendlyDate;

			// Set overdue property if due date is equal to or less than today.
			// If it's possible in future to obtain the completed status of assignments,
			// this should also check for completed === false http://bit.ly/Pt2rVn
			if (currentTime >= dueDate) {
				data[index].status = 'overdue';
			}

			// Set today/tomorrow/future/unscheduled properties.
			// Overdue items are always placed in the "today" bucket (because they're due *now*, regardless of timestamp)
			if (currentTime.toDateString() === dueDate.toDateString() || data[index].status === 'overdue') { // Today
				newData.today.push(data[index]);
			} else if (tomorrow.toDateString() === dueDate.toDateString()) { // Tomorrow
				newData.tomorrow.push(data[index]);
			} else if (dueDate > currentTime) { // Future
				newData.future.push(data[index]);
				data[index].future = true;
			} else { // Unscheduled
				newData.unscheduled.push(data[index]);
			}

		});

		// Sort each sub-array by dueDate.
		// In future, we may want to presort data rather than post (but if we have a lot of past tasks it'll be inefficient).
		// Unscheduled items get sorted by title rather than date
		var sortDate = function(a, b) {
			return parseFloat(a.real_date - b.real_date);
		};

		var sortTitle = function(a, b) {
			var alpha_a = a.title.toLowerCase();
			var alpha_b = b.title.toLowerCase();
			if (alpha_a < alpha_b){
				return -1;
			} else if (alpha_a > alpha_b) {
				return  1;
			} else {
				return 0;
			}
		};

		newData.today = newData.today.sort(sortDate);
		newData.tomorrow = newData.tomorrow.sort(sortDate);
		newData.future = newData.future.sort(sortDate);
		newData.unscheduled = newData.unscheduled.sort(sortTitle);

		var partials = {
			'taskLoop': $taskLoopTemplate.html()
		};

		calcentral.Api.Util.renderTemplate({
			'container': $tasksList,
			'data': newData,
			'partials': partials,
			'template': $tasksListTemplate
		});
	};

	var getGoogleTasks = function() {
		// Fetch tasks from Google. The @default identifier allows us to do this
		// with one API call rather than two (no need to get a list of lists first).
		// Unlike Canvas, Google returns data for anon requests - just not usable data.
		var googleTasks = $.Deferred();
		$.ajax({
			'dataType': 'json',
			'url': '/api/google/tasks/v1/lists/@default/tasks',
			'success': googleTasks.resolve,
			'error': function() {
				googleTasks.resolve({});
			}
		});
		return googleTasks;
	};

	/**
	 * Fetch user's course list from Canvas. Course IDs will be keyed against assignment
	 * course IDs to inject course titles (which missing from the Canvas upcoming API).
	 * @return {Object} Deferred promise object for a Deferrred chain, with a (data) param.
	 */
	var getCanvasCourses = function() {
		var canvasCourses = $.Deferred();
		$.ajax({
			'url': '/api/canvas/courses',
			'success': canvasCourses.resolve,
			'error': function() {
				canvasCourses.resolve({});
			}
		});
		return canvasCourses;
	};

	/**
	 * Fetch user's assignment data from Canvas.
	 * @return {Object} Deferred promise object for a Deferrred chain, with a (data) param.
	 */
	var getCanvasAssignments = function() {
		var canvasAssignments = $.Deferred();
		$.ajax({
			'url': '/api/canvas/users/self/coming_up',
			'success': canvasAssignments.resolve,
			'error': function() {
				canvasAssignments.resolve({});
			}
		});
		return canvasAssignments;
	};

	$.when(getCanvasCourses(), getCanvasAssignments(), getGoogleTasks()).done(renderTasksAssignments);
};
