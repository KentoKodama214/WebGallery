package com.web.gallery.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/** Service・Repository・Policyのメソッドシグネチャ（引数・返り値の型、引数の個数）を検証する{@link ArchCondition}を生成するユーティリティ */
final class MethodSignatureConditions {

  /**
   * メソッドの引数・返り値の型と引数の個数を検証する{@link ArchCondition}を生成する
   *
   * @param allowedArgumentPackages 引数として許容するパッケージ名の一覧
   * @param allowedReturnPackages 返り値として許容するパッケージ名の一覧（Boolean・Integerは常に許容）
   * @param allowVoidReturn 返り値としてvoidを許容する場合true
   * @param maxParameterCount 許容する引数の最大個数
   * @return 生成した{@link ArchCondition}
   */
  static ArchCondition<JavaMethod> haveAllowedSignature(
      String[] allowedArgumentPackages,
      String[] allowedReturnPackages,
      boolean allowVoidReturn,
      int maxParameterCount) {
    return new ArchCondition<JavaMethod>("have an allowed method signature") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (method.getRawParameterTypes().size() > maxParameterCount) {
          events.add(
              SimpleConditionEvent.violated(
                  method,
                  String.format(
                      "%s#%sの引数が%d個を超えています（4個以上になる場合は専用のModelクラスを定義すること）",
                      method.getOwner().getSimpleName(), method.getName(), maxParameterCount)));
        }

        for (JavaClass parameterType : method.getRawParameterTypes()) {
          if (!AllowedTypes.isInAnyPackage(parameterType, allowedArgumentPackages)) {
            events.add(
                SimpleConditionEvent.violated(
                    method,
                    String.format(
                        "%s#%sの引数の型「%s」は許容されていません",
                        method.getOwner().getSimpleName(),
                        method.getName(),
                        parameterType.getName())));
          }
        }

        JavaClass returnType = method.getRawReturnType();
        if (!AllowedTypes.isAllowedReturnType(returnType, allowVoidReturn, allowedReturnPackages)) {
          events.add(
              SimpleConditionEvent.violated(
                  method,
                  String.format(
                      "%s#%sの返り値の型「%s」は許容されていません",
                      method.getOwner().getSimpleName(), method.getName(), returnType.getName())));
        }
      }
    };
  }

  private MethodSignatureConditions() {}
}
