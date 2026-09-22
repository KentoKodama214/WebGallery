import AxeBuilder from "@axe-core/playwright";
import { expect, type Page } from "@playwright/test";

/**
 * 指定したページに対しaxe-coreの自動アクセシビリティ検査を実行し、違反がないことを検証する
 *
 * 検出漏れ（誤検知を避けるための保守的な検査）はあり得るため、機械的な検証の補助として使い、
 * 手動でのアクセシビリティ確認を代替するものではない
 *
 * @param page 検査対象のページ
 */
export async function expectNoAccessibilityViolations(page: Page): Promise<void> {
  const results = await new AxeBuilder({ page }).analyze();
  expect(results.violations, JSON.stringify(results.violations, null, 2)).toEqual([]);
}
