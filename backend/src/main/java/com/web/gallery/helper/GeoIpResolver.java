package com.web.gallery.helper;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.web.gallery.domain.common.Country;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import com.web.gallery.domain.common.Region;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * IPアドレスから国・地域を解決するHelperクラス
 *
 * <p>プライベートIP・データベース未配置・ルックアップ失敗など、解決できないケースはすべて空値（{@link IpGeoLocation#empty()}）を返す。
 * GeoIP解決の失敗がログイン・写真閲覧等の本業務を妨げてはならないため、例外は一切外へ投げない。
 *
 * <p>{@link DatabaseReader}のBean（{@code GeoIpReaderConfig}）はデータベースファイル未配置時にnullを返す。
 * これを直接コンストラクタインジェクションすると、Beanがnullを返した場合にSpringの型解決で
 * 「該当するBeanが見つからない」扱いとなり、依存する他のBean（AccountServiceImpl等）の生成自体が 失敗してしまうため、存在しない場合を許容する{@link
 * ObjectProvider}経由で取得する。
 */
@Slf4j
@Component
public class GeoIpResolver {

  /** GeoLite2データベースリーダー（データベース未配置の場合はnull） */
  private final DatabaseReader geoIpDatabaseReader;

  /**
   * コンストラクタ
   *
   * @param geoIpDatabaseReaderProvider GeoLite2データベースリーダーのProvider
   */
  public GeoIpResolver(ObjectProvider<DatabaseReader> geoIpDatabaseReaderProvider) {
    this.geoIpDatabaseReader = geoIpDatabaseReaderProvider.getIfAvailable();
  }

  /**
   * IPアドレスから国・地域を解決する
   *
   * @param ipAddress IPアドレス
   * @return {@link IpGeoLocation}（解決できない場合は{@link IpGeoLocation#empty()}）
   */
  public IpGeoLocation resolve(IpAddress ipAddress) {
    if (geoIpDatabaseReader == null) {
      return IpGeoLocation.empty();
    }

    try {
      InetAddress inetAddress = InetAddress.getByName(ipAddress.value());
      CityResponse response = geoIpDatabaseReader.city(inetAddress);
      // 国・都道府県の一方のみ判定できないケースがあるため、それぞれnull（未判定）を空文字に丸める
      String isoCode = response.getCountry().getIsoCode();
      String subdivisionName = response.getMostSpecificSubdivision().getName();
      Country country = new Country(isoCode != null ? isoCode : "");
      Region region = new Region(subdivisionName != null ? subdivisionName : "");
      return new IpGeoLocation(country, region);
    } catch (UnknownHostException | GeoIp2Exception e) {
      // プライベートIP・ローカル環境等、DB上に存在しないアドレスは日常的に発生するためdebugログに留める
      log.debug("Failed to resolve GeoIP location. (ip: {})", ipAddress.value(), e);
    } catch (IOException e) {
      log.warn("Failed to read GeoIP database. (ip: {})", ipAddress.value(), e);
    }
    return IpGeoLocation.empty();
  }
}
