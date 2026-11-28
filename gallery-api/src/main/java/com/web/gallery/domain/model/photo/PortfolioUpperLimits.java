package com.web.gallery.domain.model.photo;

import java.io.Serializable;

/**
 * 権限区分ごとのポートフォリオ作成数上限の複合値オブジェクト
 *
 * <p>special-user・administratorは上限を持たないため、mini-user・normal-userの上限のみを保持する
 *
 * @param miniUserUpperLimit MINI権限の作成数上限
 * @param normalUserUpperLimit NORMAL権限の作成数上限
 */
public record PortfolioUpperLimits(Integer miniUserUpperLimit, Integer normalUserUpperLimit)
    implements Serializable {}
