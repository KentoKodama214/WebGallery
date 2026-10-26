package com.web.gallery.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import java.util.Set;

/**
 * Service・Repository・Policyのメソッドシグネチャに許容する型を判定するユーティリティ
 *
 * <p>{@code Boolean}/{@code boolean}、{@code Integer}/{@code int}はラッパー型・プリミティブ型のどちらで宣言されていても同一視する。
 */
final class AllowedTypes {

  private static final Set<String> BOOLEAN_TYPES = Set.of("java.lang.Boolean", "boolean");
  private static final Set<String> INTEGER_TYPES = Set.of("java.lang.Integer", "int");
  private static final String VOID_TYPE = "void";

  /**
   * 指定したパッケージ配下の型かどうかを判定する（サブパッケージを含む）
   *
   * @param type 判定対象の型
   * @param packageName パッケージ名
   * @return 指定パッケージ配下の型の場合true
   */
  static boolean isInPackage(JavaClass type, String packageName) {
    String actual = type.getPackageName();
    return actual.equals(packageName) || actual.startsWith(packageName + ".");
  }

  /**
   * いずれかの許可パッケージ配下の型かどうかを判定する
   *
   * @param type 判定対象の型
   * @param allowedPackages 許可パッケージ名の一覧
   * @return いずれかの許可パッケージ配下の型の場合true
   */
  static boolean isInAnyPackage(JavaClass type, String... allowedPackages) {
    for (String packageName : allowedPackages) {
      if (isInPackage(type, packageName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Boolean・booleanかどうかを判定する
   *
   * @param type 判定対象の型
   * @return Boolean・booleanの場合true
   */
  static boolean isBoolean(JavaClass type) {
    return BOOLEAN_TYPES.contains(type.getName());
  }

  /**
   * Integer・intかどうかを判定する
   *
   * @param type 判定対象の型
   * @return Integer・intの場合true
   */
  static boolean isInteger(JavaClass type) {
    return INTEGER_TYPES.contains(type.getName());
  }

  /**
   * voidかどうかを判定する
   *
   * @param type 判定対象の型
   * @return voidの場合true
   */
  static boolean isVoid(JavaClass type) {
    return VOID_TYPE.equals(type.getName());
  }

  /**
   * 返り値の型として許容されるかどうかを判定する
   *
   * <p>許可パッケージ配下の型に加え、Boolean・Integer（件数を返す時のみ）は常に許容する。voidを許容するかどうかは呼び出し元が指定する。
   *
   * @param type 判定対象の型
   * @param allowVoid voidを許容する場合true
   * @param allowedPackages 許可パッケージ名の一覧
   * @return 許容される型の場合true
   */
  static boolean isAllowedReturnType(JavaClass type, boolean allowVoid, String... allowedPackages) {
    if (isInAnyPackage(type, allowedPackages) || isBoolean(type) || isInteger(type)) {
      return true;
    }
    return allowVoid && isVoid(type);
  }

  private AllowedTypes() {}
}
