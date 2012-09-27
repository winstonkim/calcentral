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

	/** VARIABLES. **/

	var $tasksContainer = $('#cc-widget-tasks');
	var $tasksList = $('.cc-tasks-list', $tasksContainer);
	var $tasksListTemplate = $('#cc-widget-tasks-template', $tasksContainer);
	var $taskLoopTemplate = $('#cc-taskloop-template', $tasksContainer);

	var dummy = false;

	/**
	 * Given data for task/assignments, adds additional fields to each element,
	 * and outputs new data structure organized into time sections (past/present/future)
	 * @param {object} data JSON from Canvas and bSpace
	 */
	var renderTasksAssignments = function(data) {

		var currentTime = new Date();
		// "Tomorrow" is 1 second after midnight on the next calendar date, NOT 24 hours from now.
		var tomorrow = new Date(currentTime.getFullYear(), currentTime.getMonth(), currentTime.getDate() + 1, 0, 0, 1);

		// Init an empty object into which we'll rewrite the data structure, separating tasks into today, tomorrow, future
		// We do some in-place modification of the data object first, then write the completed objects out to sections of newData.
		var newData = {
			'today': [],
			'tomorrow': [],
			'future': []
		};

		// Convert timestamps to friendly dates and set overdue flags.
		$.each(data, function(index, value){

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
				data[index].completed = true; // At least one item is completed
			}
			// END POC TEMPORARY

			var dueDate = new Date(value.start_at);
			// Javascript months are 0-based, while days and years are 1-based, so add 1 to month
			var friendlyDate = dueDate.getMonth() + 1 + '/' + dueDate.getDate();
			data[index].friendlyDate = friendlyDate;

			// Set overdue property if due date is equal to or less than today and item is uncompleted
			// if (currentTime >= dueDate && value.completed === false) {
			if (currentTime >= dueDate && completed === false) {
				data[index].overdue = true;
			}

			// Set today/tomorrow/future properties. Using .toDateString() for compares because JS' getDate() reckoning is brain-dead.
			// 8/20/2012 != 9/20/2012 Solved via http://stackoverflow.com/questions/6921606/javascript-today-function
			if (currentTime.toDateString() === dueDate.toDateString()) { // Today
				newData.today.push(data[index]);

			} else if (tomorrow.toDateString() === dueDate.toDateString()) { // Tomorrow
				newData.tomorrow.push(data[index]);

			} else if (dueDate > currentTime) {
				newData.future.push(data[index]);
				// Easier to set a "future" property here than to detect parent array in Handlebars (partials scoping problem)
				data[index].future = true;
			}
		});

		// Sort each sub-array by dueDate.
		// In future, we may want to presort data rather than post (but if we have a lot of past tasks it'll be inefficient)
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

	var getCanvasAssignments = function() {
		var $ajaxWrapper = $.Deferred();
		$.ajax({
			'url': '/api/canvas/users/self/coming_up',
			'success': function(data) {
					$ajaxWrapper.resolve(data);
				},
				'error': $ajaxWrapper.reject
			});
		return $ajaxWrapper.promise();
	};

	/**
	 * Fetch users's assignment data from canvas.
	 * @return {Object} Deferred promise object for a Deferrred chain, with a (data) param.
	 */
	var loadAssignments = function() {
		var $loadAssignmentsDeferred = $.Deferred();
		if (dummy) {
			loadDummyCourses().done($loadCoursesDeferred.resolve);
		} else {
			$.when(getCanvasAssignments()).done($loadAssignmentsDeferred.resolve).fail(function() {
				// Todo: Re-enable dummy data option (requires reformatting json to match Canvas API output)
				// loadDummyCourses().done($loadAssignmentsDeferred.resolve);
			});
		}
		return $loadAssignmentsDeferred.promise();
	};

	$.when(loadAssignments()).done(renderTasksAssignments);

};
