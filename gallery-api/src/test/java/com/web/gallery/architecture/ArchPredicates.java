package com.web.gallery.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

/**
 * 新規ArchUnitテストで共通利用する{@link DescribedPredicate}
 *
 * <p>Lombokの{@code @Builder}が生成するネストクラスや、匿名クラス・ローカルクラスはpackage単位の探索では対象パッケージの一部として
 * ヒットしてしまうが、クラス設計規約（命名・アノテーション等）のチェック対象はソースコード上のトップレベルクラスのみであるべきため、 {@link
 * #TOP_LEVEL_CLASSES}で除外する。
 */
final class ArchPredicates {

  static final DescribedPredicate<JavaClass> TOP_LEVEL_CLASSES =
      new DescribedPredicate<JavaClass>("top-level classes") {
        @Override
        public boolean test(JavaClass input) {
          return input.getEnclosingClass().isEmpty();
        }
      };

  /** レコード（{@code record}）を除いたクラスを表す */
  static final DescribedPredicate<JavaClass> NOT_RECORDS =
      new DescribedPredicate<JavaClass>("not records") {
        @Override
        public boolean test(JavaClass input) {
          return !input.isRecord();
        }
      };

  /** レコード（{@code record}）を表す */
  static final DescribedPredicate<JavaClass> RECORDS =
      new DescribedPredicate<JavaClass>("records") {
        @Override
        public boolean test(JavaClass input) {
          return input.isRecord();
        }
      };

  private ArchPredicates() {}
}
