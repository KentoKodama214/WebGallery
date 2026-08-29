/**
 * Jest用のCSS/CSS Modulesスタブ
 * `styles.foo` のようなアクセスに対してクラス名（キー名）をそのまま返す。
 */
module.exports = new Proxy(
  {},
  {
    get: (_target, key) => (key === "__esModule" ? false : key),
  }
);
