module.exports = function(grunt) {

	'use strict';

	// Project configuration.
	grunt.initConfig({
		lint: {
			all: ['grunt.js', 'src/main/webapp/widgets/**/*.js', 'src/main/webapp/js/script.js']
		},
		jshint: {
			options: {
				browser: true,
				jquery: true,
				smarttabs: true,
				strict: true,
				unused: true
			}
		}
	});

	// Default task.
	grunt.registerTask('travis', 'lint');

};
