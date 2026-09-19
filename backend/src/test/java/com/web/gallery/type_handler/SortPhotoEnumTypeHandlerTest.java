package com.web.gallery.type_handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.web.gallery.enumeration.SortPhotoEnum;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class SortPhotoEnumTypeHandlerTest {
  @InjectMocks private SortPhotoEnumTypeHandler sortPhotoEnumTypeHandler;

  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;
  @Mock private CallableStatement callableStatement;

  @Nested
  @Order(1)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class setNonNullParameter {
    @Test
    @Order(1)
    @DisplayName("正常系：PHOTO_ATを指定した場合、対応するDB保存値が設定されること")
    void setNonNullParameter_photoAt() throws SQLException {
      sortPhotoEnumTypeHandler.setNonNullParameter(
          preparedStatement, 1, SortPhotoEnum.PHOTO_AT, JdbcType.VARCHAR);

      verify(preparedStatement, times(1)).setString(1, "photo_at");
    }

    @Test
    @Order(2)
    @DisplayName("正常系：FAVORITEを指定した場合、対応するDB保存値が設定されること")
    void setNonNullParameter_favorite() throws SQLException {
      sortPhotoEnumTypeHandler.setNonNullParameter(
          preparedStatement, 1, SortPhotoEnum.FAVORITE, JdbcType.VARCHAR);

      verify(preparedStatement, times(1)).setString(1, "favorite");
    }

    @Test
    @Order(3)
    @DisplayName("正常系：SEASONを指定した場合、対応するDB保存値が設定されること")
    void setNonNullParameter_season() throws SQLException {
      sortPhotoEnumTypeHandler.setNonNullParameter(
          preparedStatement, 1, SortPhotoEnum.SEASON, JdbcType.VARCHAR);

      verify(preparedStatement, times(1)).setString(1, "season");
    }
  }

  @Nested
  @Order(2)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnName {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値がphoto_atの場合、PHOTO_ATを返すこと")
    void getNullableResultByColumnName_photoAt() throws SQLException {
      doReturn("photo_at").when(resultSet).getString("sort");

      SortPhotoEnum actual = sortPhotoEnumTypeHandler.getNullableResult(resultSet, "sort");

      assertEquals(SortPhotoEnum.PHOTO_AT, actual);
    }

    @Test
    @Order(2)
    @DisplayName("正常系：DB保存値がfavoriteの場合、FAVORITEを返すこと")
    void getNullableResultByColumnName_favorite() throws SQLException {
      doReturn("favorite").when(resultSet).getString("sort");

      SortPhotoEnum actual = sortPhotoEnumTypeHandler.getNullableResult(resultSet, "sort");

      assertEquals(SortPhotoEnum.FAVORITE, actual);
    }

    @Test
    @Order(3)
    @DisplayName("正常系：DB保存値がseasonの場合、SEASONを返すこと")
    void getNullableResultByColumnName_season() throws SQLException {
      doReturn("season").when(resultSet).getString("sort");

      SortPhotoEnum actual = sortPhotoEnumTypeHandler.getNullableResult(resultSet, "sort");

      assertEquals(SortPhotoEnum.SEASON, actual);
    }

    @Test
    @Order(4)
    @DisplayName("異常系：DB値がnullの場合、nullを返すこと")
    void getNullableResultByColumnName_null() throws SQLException {
      doReturn(null).when(resultSet).getString("sort");

      SortPhotoEnum actual = sortPhotoEnumTypeHandler.getNullableResult(resultSet, "sort");

      assertNull(actual);
    }

    @Test
    @Order(5)
    @DisplayName("異常系：DB保存値に一致するEnum値がない場合、nullを返すこと")
    void getNullableResultByColumnName_unmatched() throws SQLException {
      doReturn("unknown").when(resultSet).getString("sort");

      SortPhotoEnum actual = sortPhotoEnumTypeHandler.getNullableResult(resultSet, "sort");

      assertNull(actual);
    }
  }

  @Nested
  @Order(3)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByColumnIndex {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致するEnum値を返すこと")
    void getNullableResultByColumnIndex_matched() throws SQLException {
      doReturn("photo_at").when(resultSet).getString(1);

      SortPhotoEnum actual = sortPhotoEnumTypeHandler.getNullableResult(resultSet, 1);

      assertEquals(SortPhotoEnum.PHOTO_AT, actual);
    }
  }

  @Nested
  @Order(4)
  @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
  class getNullableResultByCallableStatement {
    @Test
    @Order(1)
    @DisplayName("正常系：DB保存値に一致するEnum値を返すこと")
    void getNullableResultByCallableStatement_matched() throws SQLException {
      doReturn("favorite").when(callableStatement).getString(1);

      SortPhotoEnum actual = sortPhotoEnumTypeHandler.getNullableResult(callableStatement, 1);

      assertEquals(SortPhotoEnum.FAVORITE, actual);
    }
  }
}
