const path = require('path');

module.exports = {
    entry: './webpack/index.js',
    output: {
	filename: 'bundle.js',
	path: path.resolve(__dirname, '../export/main/js/compiled/'),
    },
    resolve: {
	alias: {
	    'safe-email': path.resolve(__dirname, '../node_modules/safe-email/dist/safe-email.js')
	}
    },
};
