import AxeBuilder from "@axe-core/playwright";
import { expect, type Page } from "@playwright/test";

interface AccessibilityCheckOptions {
  /**
   * 検査から除外するaxeルールID
   *
   * 既知の未対応項目を一時的に除外するためだけに使う。除外する場合は、呼び出し側に
   * 「なぜ除外するのか・いつ解消するのか」をコメントで必ず残すこと
   */
  disableRules?: string[];
}

/**
 * 指定したページに対しaxe-coreの自動アクセシビリティ検査を実行し、違反がないことを検証する
 *
 * 検出漏れ（誤検知を避けるための保守的な検査）はあり得るため、機械的な検証の補助として使い、
 * 手動でのアクセシビリティ確認を代替するものではない
 *
 * @param page 検査対象のページ
 * @param options 検査オプション
 */
export async function expectNoAccessibilityViolations(
  page: Page,
  options: AccessibilityCheckOptions = {}
): Promise<void> {
  let builder = new AxeBuilder({ page });
  if (options.disableRules && options.disableRules.length > 0) {
    builder = builder.disableRules(options.disableRules);
  }
  const results = await builder.analyze();
  expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([]);
}
