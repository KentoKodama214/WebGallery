# WebGallery アーキテクチャ設計書

## 概要

WebGalleryは、Spring Bootベースのレイヤード・アーキテクチャを採用しています。

## ドキュメント一覧

- [レイヤード・アーキテクチャ](./layered-architecture.md)
- [セキュリティ](./security.md)
- [モジュール構成・依存関係（Spring Modulith）](../modulith/components.png)

## Spring Modulithドキュメントの図を生成する手順

PlantUMLファイル（`.puml`）をPNG画像に変換する手順を示す。

### 前提条件

```bash
brew install plantuml
```

### PNGへの変換

```bash
# 全ファイルをPNGに変換
plantuml doc/modulith/*.puml
```

`doc/modulith/` 配下に各 `.puml` ファイルに対応する `.png` ファイルが生成される。
