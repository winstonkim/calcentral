module.exports = function(grunt) {

	// Project configuration.
	grunt.initConfig({
		lint: {
			all: ['src/main/webapp/widgets/**/*.js', 'src/main/webapp/js/script.js']
		},
		jshint: {
			options: {
				browser: true,
				smarttabs: true
			}
		}
	});

	// Default task.
	grunt.registerTask('travis', 'lint');

};
