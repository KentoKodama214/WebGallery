---
paths:
  - backend/**/controller/**
---

# Controller層のアーキテクチャルール

## 命名規則

- クラス名サフィックス: `Controller` または `RestController` または `RestControllerAdvice`

## APIルートの一元管理

- APIパスを文字列リテラルとして直接記述しないこと
- `@RequestMapping`、`@GetMapping`、`@PostMapping`、`@PutMapping`、`@DeleteMapping`等のパスは`ApiRoutes`クラスの定数を参照すること

## レイヤー間依存関係

- **許可するimport**: `service/`のインターフェース、`model/`、`controller/request/`、`controller/response/`、`constant/`
- **禁止するimport**: `repository/`、`mapper/`、`entity/`、`dto/`、`service/impl/`への直接依存

## Responseファクトリメソッド経由の呼び出し

- Controller内でResponseオブジェクトを直接`new`やビルダーで生成しないこと
- Responseクラスのファクトリメソッド（`from()`、`of()`）を経由して生成すること
