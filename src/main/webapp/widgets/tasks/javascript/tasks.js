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
	 * @param {object} data Assignment list from Canvas
	 * @param {object} gTaskData list from Google
	 */
	var renderTasksAssignments = function(courseData, data, gTaskData) {
		// To account for differences between tasks emitted from various services, set an "emitter"
		// property for the source. This will help in setting different icons, different date processing, etc.
		$.each(data, function(index, value){
			value.emitter = 'canvas-assignments';
		});

		// Merge Google tasks data into the main data object.
		// Modify Google task properties to re-use Canvas assignment properties for compatibility.
		$.each(gTaskData[0].items, function(index, value){
			// Discard "phantom" Google Tasks
			if (value.title.match('^[a-zA-Z0-9]')) {
				// Google "due" property maps to Canvas "start_at" property
				if (value.due) {
					value.start_at = value.due;
				}
				value.html_url = 'https://mail.google.com/tasks/canvas?pli=1'; // Minimal, but it's the only Tasks web UI available
				value.emitter = 'google-tasks';

				// Append modified entry to main data object
				data.push(value);
			}
		});

		var currentTime = new Date();
		// "Tomorrow" is 1 second after midnight on the next calendar date, NOT 24 hours from now.
		var tomorrow = new Date(currentTime.getFullYear(), currentTime.getMonth(), currentTime.getDate() + 1, 0, 0, 1);

		// Init an empty object into which we'll rewrite the data structure, separating tasks into today, tomorrow, future
		// We do some in-place modification of the data object first, then write the completed objects out to sections of newData.
		var newData = {
			'today': [],
			'tomorrow': [],
			'future': [],
			'undated': []
		};

		// Convert timestamps to friendly dates and set overdue flags.
		$.each(data, function(index, value){
			// For Canvas items, set the html_url
			if (data[index].html_url) {
				// Grep out this assignment's course ID and URL; set matching course properties for assignments.
				var courseId = parseInt(data[index].html_url.match(/\d+/g)[0], 10);
				var courseURL = data[index].html_url.match(/^.*\d+\//g);
				data[index].sourceUrl = courseURL;
				$.each(courseData, function(i, v){
					if (v.id === courseId) {
						data[index].source = v.course_code;
					}
				});
			}

			// **** TODO: POC ONLY **** monkey-patch dates so we always have items for today, tomorrow, and future
			// Ignore the stored timestamps and dynamically generate new ones at a variety of ranges.
			var theDateEpoch = currentTime.getTime() / 1000;

			if (index < 1) {
				data[index].overdue = true; // Set at least one item to overdue
			} else if (index < 3) {
				data[index].dueDate = theDateEpoch; // Today
			} else if (index < 5) {
				data[index].dueDate = theDateEpoch + 86400; // Tomorrow
			} else if (index < 7) {
				data[index].dueDate = theDateEpoch + 172800; // Upcoming
			} else {
				data[index].dueDate = theDateEpoch + 1672800; // Far future
			}

			if (index === 1) {
				data[index].completed = true; // At least one item is completed for demo purposes
				// data[index].overdue = false; // This same item should not be both overdue and completed
			}
			// END POC TEMPORARY


			// Set due dates and overdue status for items that are dated.
			if (value.start_at || value.due) {
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
					data[index].overdue = true;
				}

				// Set today/tomorrow/future/undated properties. Using .toDateString() for compares because JS' getDate() reckoning is brain-dead.
				// 8/20/2012 != 9/20/2012 Solved via http://stackoverflow.com/questions/6921606/javascript-today-function
				if (currentTime.toDateString() === dueDate.toDateString()) { // Today
					newData.today.push(data[index]);

				} else if (tomorrow.toDateString() === dueDate.toDateString()) { // Tomorrow
					newData.tomorrow.push(data[index]);

				} else if (dueDate > currentTime) {
					newData.future.push(data[index]);
					data[index].future = true;
				}
			} else {
				// Everything else is undated ... but don't display completed Google tasks
				if (data[index].status !== 'completed') {
					newData.undated.push(data[index]);
				}
			}
		});

		// Sort each sub-array by dueDate.
		// In future, we may want to presort data rather than post (but if we have a lot of past tasks it'll be inefficient).
		// Undated items go through unsorted (change in future?)
		var sortDate = function(a, b) {
			return parseFloat(a.dueDate - b.dueDate);
		};

		newData.today = newData.today.sort(sortDate);
		newData.tomorrow = newData.tomorrow.sort(sortDate);
		newData.future = newData.future.sort(sortDate);

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
		var getGoogleLists = $.ajax({
				'dataType': 'json',
				'url': '/api/google/tasks/v1/lists/@default/tasks'
			});
		return getGoogleLists;
	};

	/**
	 * Fetch user's course list from Canvas. Course IDs will be keyed against assignment
	 * course IDs to inject course titles (which missing from the Canvas upcoming API).
	 * @return {Object} Deferred promise object for a Deferrred chain, with a (data) param.
	 */
	var getCanvasCourses = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'url': '/api/canvas/courses',
			'success': function(courseData) {
				$ajaxWrapper.resolve(courseData);
			},
			'error': $ajaxWrapper.reject
		});
		return $ajaxWrapper;
	};

	/**
	 * Fetch user's assignment data from Canvas.
	 * @return {Object} Deferred promise object for a Deferrred chain, with a (data) param.
	 */
	var getCanvasAssignments = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'url': '/api/canvas/users/self/coming_up',
			'success': function(data) {
				$ajaxWrapper.resolve(data);
			},
			'error': $ajaxWrapper.reject
		});
		return $ajaxWrapper;
	};

	$.when(getCanvasCourses(), getCanvasAssignments(), getGoogleTasks()).done(renderTasksAssignments).fail(function() {
		console.log("tasks.js -> Could not load assignment or course data from Canvas");
	});
};
