---
paths:
  - backend/**/entity/**
---

# Entityクラスのアーキテクチャルール

## Lombokアノテーション規約

- **許可**: `@Data` と `@Builder` のみ
- **禁止**: `@NoArgsConstructor`、`@AllArgsConstructor`、`@Value`、`@Getter`、`@Setter`

## プロパティ

- Modelクラス、Dtoクラスは使用しない（依存性・責務の分離のため）
- ドメインクラス（値オブジェクト）は`type_handler/`のTypeHandlerを介してプロパティに使用してよい

## 抽出条件・更新対象クラスの分離

- Entityクラス自体は「永続化Entity」に専念させ、DBレコードのマッピング（resultMap）と登録（insert）用のファクトリメソッドのみを持つ
- MyBatisのSELECT/DELETE等の抽出条件（WHERE句）は、Entityとは別クラスに分離する
- UPDATEのSET句（更新対象）がある場合も、Entityとは別クラスに分離する
- 1つのクラスが「永続化Entity」「抽出条件」「更新対象」を兼務しないこと（単一責任原則のため）

## クラス名の命名規約

- 抽出条件クラス: `<Entity名>Condition`（例: `PhotoMstCondition`, `AccountCondition`）
- 更新対象クラス: `<Entity名>UpdateTarget`（例: `PhotoMstUpdateTarget`, `AccountUpdateTarget`）
- UPDATE操作を持たないEntityの場合、更新対象クラスは作成せず抽出条件クラスのみを分離する
