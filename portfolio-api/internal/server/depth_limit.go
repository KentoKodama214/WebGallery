package server

import (
	"context"

	"github.com/99designs/gqlgen/graphql"
	"github.com/vektah/gqlparser/v2/ast"
	"github.com/vektah/gqlparser/v2/gqlerror"
)

// DepthLimit は GraphQL クエリのネストの深さを制限する gqlgen の拡張。
//
// 複雑度の上限（extension.FixedComplexityLimit）だけでは、各階層の要素数が少ない深いネストを
// 十分に抑えられないため、深さも別に制限する。フラグメントは展開した深さで数える。
type DepthLimit struct {
	Max int
}

var _ interface {
	graphql.HandlerExtension
	graphql.OperationContextMutator
} = DepthLimit{}

// ExtensionName は拡張の名前を返す。
func (DepthLimit) ExtensionName() string { return "DepthLimit" }

// Validate はスキーマに対する拡張の前提条件を検証する（前提条件はない）。
func (DepthLimit) Validate(graphql.ExecutableSchema) error { return nil }

// MutateOperationContext はクエリの深さが上限を超える場合にエラーを返し、実行させない。
func (d DepthLimit) MutateOperationContext(_ context.Context, oc *graphql.OperationContext) *gqlerror.Error {
	if oc.Operation == nil {
		return nil
	}
	if depth := selectionSetDepth(oc.Operation.SelectionSet, map[string]bool{}); depth > d.Max {
		return gqlerror.Errorf("query depth %d exceeds the limit of %d", depth, d.Max)
	}
	return nil
}

// selectionSetDepth は選択セットの深さ（フィールドの階層数）を返す。
// visiting はフラグメントの循環参照で無限に再帰しないための訪問中のフラグメント名（検証済みのクエリでは循環しない）。
func selectionSetDepth(selectionSet ast.SelectionSet, visiting map[string]bool) int {
	maxDepth := 0
	for _, selection := range selectionSet {
		depth := 0
		switch s := selection.(type) {
		case *ast.Field:
			depth = 1 + selectionSetDepth(s.SelectionSet, visiting)
		case *ast.InlineFragment:
			depth = selectionSetDepth(s.SelectionSet, visiting)
		case *ast.FragmentSpread:
			if s.Definition == nil || visiting[s.Name] {
				continue
			}
			visiting[s.Name] = true
			depth = selectionSetDepth(s.Definition.SelectionSet, visiting)
			delete(visiting, s.Name)
		}
		if depth > maxDepth {
			maxDepth = depth
		}
	}
	return maxDepth
}
