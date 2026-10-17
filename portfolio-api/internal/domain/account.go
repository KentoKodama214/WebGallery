package domain

import "regexp"

// accountIDPattern はアカウントIDの形式（半角英数字8〜20文字。Java 側・フロントエンドの検証と一致させる）。
var accountIDPattern = regexp.MustCompile(`^[a-zA-Z0-9]{8,20}$`)

// IsValidAccountID はアカウントIDの形式が正しいかどうかを判定する。
func IsValidAccountID(accountID string) bool {
	return accountIDPattern.MatchString(accountID)
}
