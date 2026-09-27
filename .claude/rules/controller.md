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

- **許可するimport**: `application/service/`のインターフェース、`application/model/`、`presentation/request/`、`presentation/response/`、`presentation/converter/`、`domain/constant/`、`infrastructure/`配下（`SessionHelper`、`ClientIpResolver`等、presentationからinfrastructureへの依存は一方向のみ許可される。詳細は`architecture-overview.md`）
- **禁止するimport**: `infrastructure/persistence/repository・mapper・entity・dto`、`application/service/impl/`への直接依存

## Request → Modelの変換

- ControllerはRequestオブジェクトから直接Modelを組み立てない。`presentation/converter/`の専用Converterクラス（例: `PhotoConverter`）を`@Component`としてDIし、そのメソッド経由でRequestをModelへ変換すること
- Converterクラスの詳細な設計ルールは`converter.md`を参照

## Responseファクトリメソッド経由の呼び出し

- Controller内でResponseオブジェクトを直接`new`やビルダーで生成しないこと
- Responseクラスのファクトリメソッド（`from()`、`of()`）を経由して生成すること

## 検証

本ファイルのルールは`backend/src/test/java/com/web/gallery/architecture/ControllerArchitectureTest.java`のArchUnitテストで機械的に検証される。ただし「APIパスを文字列リテラルとして直接記述しない」ルールは、コンパイル後のバイトコードでは定数参照と直書きの区別がつかないため厳密な検証はできず、代わりに「マッピングパスの値が`ApiRoutes`クラスの定数値のいずれかと一致するか」を近似的に検証する。
