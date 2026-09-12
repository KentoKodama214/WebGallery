package com.web.gallery.architecture;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleName;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;

/**
 * {@code .claude/rules/model.md}に定義されたModelクラスのアーキテクチャルールを検証するテスト
 *
 * <p>{@code @Value}/{@code @Builder}等のLombokアノテーション規約は、コンパイル時にコンパイラが完全に除去する{@code
 * RetentionPolicy.SOURCE}のためバイトコード解析であるArchUnitでは検証不可能であり、本クラスの対象外とする（{@code
 * backend-architecture-checker}によるレビューで担保する）。{@code @NonNull}（{@code
 * RetentionPolicy.CLASS}）はバイトコードに残るため検証可能。
 *
 * <p>ファーストクラスコレクション（{@code XxxModelList}）はLombokを使用しないrecordのため、通常のModelクラスとは別条件で検証する。
 */
@AnalyzeClasses(packages = "com.web.gallery", importOptions = ImportOption.DoNotIncludeTests.class)
class ModelArchitectureTest {

  /**
   * AccountModelは部分更新用の複数のファクトリメソッド（forUnlock等）を持ち、全ファクトリメソッドに共通して必須となる
   * プロパティが存在しないため、意図的に{@code @NonNull}を付与していない（{@link
   * com.web.gallery.model.AccountModel}のクラスJavadoc参照）
   */
  private static final String DOCUMENTED_NON_NULL_EXCEPTION = "AccountModel";

  @ArchTest
  static final ArchRule modelClassShouldHaveAtLeastOneNonNullField =
      classes()
          .that()
          .resideInAPackage(Packages.MODEL)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .and(ArchPredicates.NOT_RECORDS)
          .and(not(simpleName(DOCUMENTED_NON_NULL_EXCEPTION)))
          .should(FieldAnnotationConditions.haveAtLeastOneFieldAnnotatedWith("lombok.NonNull"))
          .as("Modelクラスには@NonNullが付与されたフィールドが最低1つ存在すること");

  @ArchTest
  static final ArchRule modelListShouldBeIterableRecord =
      classes()
          .that()
          .resideInAPackage(Packages.MODEL)
          .and(ArchPredicates.TOP_LEVEL_CLASSES)
          .and()
          .haveSimpleNameEndingWith("ModelList")
          .should(ArchCondition.<JavaClass>from(ArchPredicates.RECORDS))
          .andShould()
          .implement(Iterable.class)
          .as("XxxModelListクラスはrecordかつIterableを実装すること");
}
