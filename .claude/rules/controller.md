---
paths:
  - backend/**/controller/**
---

# Controller層のアーキテクチャルール

## 命名規則

- クラス名サフィックス: `Controller` または `ControllerAdvice`

## APIルートの一元管理

- APIパスを文字列リテラルとして直接記述しないこと
- `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`等のパスは`ApiRoutes`クラスの定数を参照すること

## レイヤー間依存関係

- **許可するimport**: `service/`のインターフェース、`model/`、`controller/request/`、`controller/response/`、`constant/`
- **禁止するimport**: `repository/`、`mapper/`、`entity/`、`dto/`、`service/impl/`への直接依存

## Responseファクトリメソッド経由の呼び出し

- Controller内でResponseオブジェクトを直接`new`やビルダーで生成しないこと
- Responseクラスのファクトリメソッド（`from()`、`of()`）を経由して生成すること

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/ControllerArchitectureTest.java`のArchUnitテストで機械的に検証される。ただし「APIパスを文字列リテラルとして直接記述しない」ルールは、コンパイル後のバイトコードでは定数参照と直書きの区別がつかないため厳密な検証はできず、代わりに「マッピングパスの値が`ApiRoutes`クラスの定数値のいずれかと一致するか」を近似的に検証する。
