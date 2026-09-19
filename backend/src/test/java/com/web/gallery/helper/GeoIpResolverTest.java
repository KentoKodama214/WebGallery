package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.Subdivision;
import com.web.gallery.domain.common.IpAddress;
import com.web.gallery.domain.common.IpGeoLocation;
import java.io.IOException;
import java.net.InetAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GeoIpResolverTest {

  @SuppressWarnings("unchecked")
  private ObjectProvider<DatabaseReader> providerReturning(DatabaseReader databaseReader) {
    ObjectProvider<DatabaseReader> provider = mock(ObjectProvider.class);
    doReturn(databaseReader).when(provider).getIfAvailable();
    return provider;
  }

  @Test
  @Order(1)
  @DisplayName("異常系：DatabaseReaderのBeanが存在しない場合、空値を返すこと")
  void resolve_databaseReaderUnavailable() {
    GeoIpResolver geoIpResolver = new GeoIpResolver(providerReturning(null));

    IpGeoLocation actual = geoIpResolver.resolve(new IpAddress("203.0.113.1"));

    assertEquals(IpGeoLocation.empty(), actual);
  }

  @Test
  @Order(2)
  @DisplayName("正常系：国・地域が解決できる場合、対応するIpGeoLocationを返すこと")
  void resolve_success() throws IOException, GeoIp2Exception {
    DatabaseReader databaseReader = mock(DatabaseReader.class);
    CityResponse cityResponse = mock(CityResponse.class);
    Country country = mock(Country.class);
    Subdivision subdivision = mock(Subdivision.class);
    doReturn(country).when(cityResponse).getCountry();
    doReturn(subdivision).when(cityResponse).getMostSpecificSubdivision();
    doReturn("JP").when(country).getIsoCode();
    doReturn("Tokyo").when(subdivision).getName();
    doReturn(cityResponse).when(databaseReader).city(any(InetAddress.class));

    GeoIpResolver geoIpResolver = new GeoIpResolver(providerReturning(databaseReader));

    IpGeoLocation actual = geoIpResolver.resolve(new IpAddress("203.0.113.1"));

    assertEquals("JP", actual.country().value());
    assertEquals("Tokyo", actual.region().value());
  }

  @Test
  @Order(3)
  @DisplayName("正常系：国コードが判定できない場合、国コードを空文字にすること")
  void resolve_countryIsoCodeNull() throws IOException, GeoIp2Exception {
    DatabaseReader databaseReader = mock(DatabaseReader.class);
    CityResponse cityResponse = mock(CityResponse.class);
    Country country = mock(Country.class);
    Subdivision subdivision = mock(Subdivision.class);
    doReturn(country).when(cityResponse).getCountry();
    doReturn(subdivision).when(cityResponse).getMostSpecificSubdivision();
    doReturn(null).when(country).getIsoCode();
    doReturn("Tokyo").when(subdivision).getName();
    doReturn(cityResponse).when(databaseReader).city(any(InetAddress.class));

    GeoIpResolver geoIpResolver = new GeoIpResolver(providerReturning(databaseReader));

    IpGeoLocation actual = geoIpResolver.resolve(new IpAddress("203.0.113.1"));

    assertEquals("", actual.country().value());
    assertEquals("Tokyo", actual.region().value());
  }

  @Test
  @Order(4)
  @DisplayName("正常系：地域名が判定できない場合、地域名を空文字にすること")
  void resolve_subdivisionNameNull() throws IOException, GeoIp2Exception {
    DatabaseReader databaseReader = mock(DatabaseReader.class);
    CityResponse cityResponse = mock(CityResponse.class);
    Country country = mock(Country.class);
    Subdivision subdivision = mock(Subdivision.class);
    doReturn(country).when(cityResponse).getCountry();
    doReturn(subdivision).when(cityResponse).getMostSpecificSubdivision();
    doReturn("JP").when(country).getIsoCode();
    doReturn(null).when(subdivision).getName();
    doReturn(cityResponse).when(databaseReader).city(any(InetAddress.class));

    GeoIpResolver geoIpResolver = new GeoIpResolver(providerReturning(databaseReader));

    IpGeoLocation actual = geoIpResolver.resolve(new IpAddress("203.0.113.1"));

    assertEquals("JP", actual.country().value());
    assertEquals("", actual.region().value());
  }

  @Test
  @Order(5)
  @DisplayName("異常系：DB上にアドレスが存在しない場合、空値を返すこと")
  void resolve_addressNotFound() throws IOException, GeoIp2Exception {
    DatabaseReader databaseReader = mock(DatabaseReader.class);
    doThrow(new AddressNotFoundException("not found")).when(databaseReader).city(any());

    GeoIpResolver geoIpResolver = new GeoIpResolver(providerReturning(databaseReader));

    IpGeoLocation actual = geoIpResolver.resolve(new IpAddress("203.0.113.1"));

    assertEquals(IpGeoLocation.empty(), actual);
  }

  @Test
  @Order(6)
  @DisplayName("異常系：データベース読み込みに失敗した場合、空値を返すこと")
  void resolve_ioException() throws IOException, GeoIp2Exception {
    DatabaseReader databaseReader = mock(DatabaseReader.class);
    doThrow(new IOException("read error")).when(databaseReader).city(any());

    GeoIpResolver geoIpResolver = new GeoIpResolver(providerReturning(databaseReader));

    IpGeoLocation actual = geoIpResolver.resolve(new IpAddress("203.0.113.1"));

    assertEquals(IpGeoLocation.empty(), actual);
  }
}
