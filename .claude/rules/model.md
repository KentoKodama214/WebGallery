---
paths:
  - backend/**/application/model/**
---

# Modelクラスのアーキテクチャルール

`application/model/`に配置する。オニオンアーキテクチャ上、Modelクラス（`application`層）が`presentation/request/`のRequestクラスや`infrastructure/persistence/entity・dto`のEntity/Dtoクラス（いずれも外側の層）を直接参照することは依存性逆転となるため禁止する。変換ロジックは以下のいずれかに切り出すこと。

- **Request → Model**: `presentation/converter/`の専用Converterクラス（`@Component`）にメソッドとして実装する（詳細は`converter.md`）。Modelクラス自身に`from(Request)`のような静的ファクトリメソッドを持たせない
- **Dto/Entity → Model**: 対応する`infrastructure/persistence/repository/`のRepository実装クラス内のprivateメソッド（例: `toXxxModel(XxxDto dto)`）として実装する。Dto/Entity→Model変換専用のクラスは設けない（詳細は`repository.md`）

## 命名規則

- クラス名サフィックス: `Model`
- Modelのコレクションを表すクラスのクラス名サフィックス: `ModelList`（例：`AccountModelList`、`PhotoModelList`）
- 例外：`domain/`配下の値オブジェクトのコレクションを表すクラスは、`Model`を含まない`XxxList`とする（例：`PhotoNoList`）

## Lombokアノテーション規約

- **許可**: `@Value` と `@Builder` のみ
- **禁止**: `@NoArgsConstructor`、`@AllArgsConstructor`、`@Data`、`@Getter`、`@Setter`

## @NonNullアノテーション

- Null許容しないプロパティには`@NonNull`アノテーションを付与すること
- `@NonNull`が一つも使われていないModelクラスは違反の可能性がある
  - 例外：`AccountModel`は部分更新用の複数のファクトリメソッド（`forUnlock`等）を持ち、全ファクトリメソッドに共通して必須となるプロパティが存在しないため、意図的に`@NonNull`を使用しない

## コレクションオブジェクト（ファーストクラスコレクション、`XxxModelList`）

- `record XxxModelList(List<XxxModel> xxxModelList) implements Iterable<XxxModel>`として実装する（Lombokの`@Value`/`@Builder`は使用しない）
- コンパクトコンストラクタで`Objects.requireNonNull()`によるnullチェックを行う
- クラスのJavadocコメントに、レコードコンポーネントを説明する`@param`タグを付与すること（checkstyleのJavadocTypeルールで必須）
- ソート機能・フィルター機能はインスタンスメソッドとして提供し、新しい`XxxModelList`を返すこと（元のインスタンスを変更しない）
- ファクトリメソッドとして、Modelのリストから生成する`of()`、空インスタンスを生成する`empty()`を提供すること（Entity/Dto/Requestからの変換は上記の通りConverter・Repository実装側の責務であり、`XxxModelList`自体に`from()`は持たせない）
- `size()`、`isEmpty()`、`get(int)`、`stream()`、`toList()`を提供し、`iterator()`をオーバーライドすること

## 検証

`@NonNull`の付与、および`XxxModelList`がrecordかつ`Iterable`を実装していることは`backend/src/test/java/com/web/gallery/architecture/ModelArchitectureTest.java`のArchUnitテストで機械的に検証される。ただし`@Value`/`@Builder`のLombokアノテーション規約は、コンパイラが`RetentionPolicy.SOURCE`で完全に除去しバイトコードに一切残らないため、バイトコード解析であるArchUnitでは原理的に検証不可能であり、`backend-architecture-checker`によるレビューで担保する。
