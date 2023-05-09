const path = require('path');

module.exports = {
  entry: './webpack/index.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, '../export/main/js/compiled/'),
  },
};
