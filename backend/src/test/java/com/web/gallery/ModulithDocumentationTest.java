package com.web.gallery;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulithのドキュメント生成テスト。
 *
 * <p>モジュール構成図（PlantUML）とモジュールキャンバス（AsciiDoc）を {@code doc/modulith/} 配下に自動生成する。
 *
 * <p>実行方法: {@code ./backend/gradlew -p backend generateModulithDocs}
 */
class ModulithDocumentationTest {

  /** ドキュメント出力先ディレクトリ（backendディレクトリからの相対パス） */
  private static final String OUTPUT_DIR = "../doc/modulith";

  @Test
  void generateModulithDocumentation() {
    var modules = ApplicationModules.of(WebGalleryApplication.class);
    var options = Documenter.Options.defaults().withOutputFolder(OUTPUT_DIR);

    new Documenter(modules, options)
        .writeModulesAsPlantUml()
        .writeIndividualModulesAsPlantUml()
        .writeModuleCanvases()
        .writeAggregatingDocument();
  }
}
