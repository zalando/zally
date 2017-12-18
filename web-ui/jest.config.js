module.exports = {
  globals: {
    window: true,
  },
  setupFiles: ['./jest.setup.js'],
  collectCoverageFrom: ['src/server/**/*.js', 'src/client/app/**/*.{js,jsx}'],
  coverageDirectory: 'target/coverage',
  coverageReporters: ['json', 'lcov', 'html'],
};
