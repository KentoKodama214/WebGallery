/** @type {import('jest').Config} */
const config = {
  testEnvironment: "jsdom",
  transform: {
    "^.+\\.(ts|tsx)$": [
      "ts-jest",
      {
        tsconfig: "tsconfig.json",
      },
    ],
  },
  moduleNameMapper: {
    // CSS / CSS Modules のインポートをスタブ化する
    "\\.(css|scss|sass|less)$": "<rootDir>/test/styleMock.js",
    // Server Component 専用のモジュールの目印（Next.js がビルド時に解決する）をスタブ化する
    "^server-only$": "<rootDir>/test/serverOnlyMock.js",
    "^@/(.*)$": "<rootDir>/src/$1",
  },
  testMatch: ["**/__tests__/**/*.(ts|tsx)", "**/?(*.)+(spec|test).(ts|tsx)"],
  testPathIgnorePatterns: ["/node_modules/", "/e2e/"],
  collectCoverageFrom: [
    "src/**/*.{ts,tsx}",
    "!src/**/*.d.ts",
    "!src/**/__tests__/**",
  ],
  coverageReporters: ["text", "json-summary"],
};

module.exports = config;
