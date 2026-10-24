/**
 * portfolio-api に送る GraphQL クエリ
 *
 * スキーマの正本は `portfolio-api/graph/schema.graphqls`。フィールドを追加・変更した場合は、
 * `types.ts` の型も合わせて更新すること。
 */

const PHOTO_FIELDS = "photoNo url japaneseTitle englishTitle direction";

/** 公開中のポートフォリオ一覧 */
export const PUBLISHED_PORTFOLIOS_QUERY = `query PublishedPortfolios($accountId: ID!) {
  publishedPortfolios(accountId: $accountId) {
    portfolioNo theme description template photoCount updatedAt
    tags { japaneseName englishName }
    coverPhoto { ${PHOTO_FIELDS} }
  }
}`;

/** 公開中のポートフォリオ（テンプレートごとのレイアウトを含む） */
export const PUBLISHED_PORTFOLIO_QUERY = `query PublishedPortfolio($accountId: ID!, $portfolioNo: Int!) {
  publishedPortfolio(accountId: $accountId, portfolioNo: $portfolioNo) {
    portfolioNo theme description template updatedAt
    tags { japaneseName englishName }
    layout {
      __typename
      ... on FeatureLayout { sections { theme subTheme description photo { ${PHOTO_FIELDS} } } }
      ... on GridLayout { rows { pattern photos { photo { ${PHOTO_FIELDS} } } } }
    }
  }
}`;
