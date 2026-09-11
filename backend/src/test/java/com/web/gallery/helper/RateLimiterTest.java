package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

/** {@link RateLimiter} のユニットテスト */
@ActiveProfiles("test")
class RateLimiterTest {

  /** 現在時刻を進められるテスト用Clock */
  private static final class MutableClock extends Clock {
    private long millis;

    private MutableClock(long startMillis) {
      this.millis = startMillis;
    }

    @Override
    public long millis() {
      return millis;
    }

    void advanceSeconds(long seconds) {
      millis += seconds * 1_000L;
    }

    @Override
    public Instant instant() {
      return Instant.ofEpochMilli(millis);
    }

    @Override
    public ZoneId getZone() {
      return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }
  }

  private MutableClock clock;

  private RateLimiter rateLimiter;

  private static final RateLimiter.Rule RULE = new RateLimiter.Rule(3, Duration.ofSeconds(60));

  @BeforeEach
  void setUp() {
    clock = new MutableClock(1_000_000_000_000L);
    rateLimiter = new RateLimiter(clock);
  }

  @Test
  @DisplayName("ウィンドウ内で上限まで許可し、超過分を拒否する")
  void allowsUpToCapacity() {
    assertTrue(rateLimiter.tryAcquire("ip|AUTH", RULE));
    assertTrue(rateLimiter.tryAcquire("ip|AUTH", RULE));
    assertTrue(rateLimiter.tryAcquire("ip|AUTH", RULE));

    assertFalse(rateLimiter.tryAcquire("ip|AUTH", RULE));
  }

  @Test
  @DisplayName("ウィンドウを跨ぐとカウントがリセットされる")
  void resetsAfterWindow() {
    assertTrue(rateLimiter.tryAcquire("ip|AUTH", RULE));
    assertTrue(rateLimiter.tryAcquire("ip|AUTH", RULE));
    assertTrue(rateLimiter.tryAcquire("ip|AUTH", RULE));
    assertFalse(rateLimiter.tryAcquire("ip|AUTH", RULE));

    clock.advanceSeconds(60);

    assertTrue(rateLimiter.tryAcquire("ip|AUTH", RULE));
  }

  @Test
  @DisplayName("キーが異なれば別々にカウントする")
  void countsPerKey() {
    assertTrue(rateLimiter.tryAcquire("ip-a|AUTH", RULE));
    assertTrue(rateLimiter.tryAcquire("ip-a|AUTH", RULE));
    assertTrue(rateLimiter.tryAcquire("ip-a|AUTH", RULE));
    assertFalse(rateLimiter.tryAcquire("ip-a|AUTH", RULE));

    assertTrue(rateLimiter.tryAcquire("ip-b|AUTH", RULE));
    assertTrue(
        rateLimiter.tryAcquire("ip-a|GENERAL", new RateLimiter.Rule(3, Duration.ofSeconds(60))));
  }

  @Test
  @DisplayName("十分に古いエントリは間引かれる")
  void sweepsStaleEntries() {
    assertTrue(rateLimiter.tryAcquire("ip-old|AUTH", RULE));

    // STALE_THRESHOLD_MILLIS（1時間）＋ SWEEP_MIN_INTERVAL_MILLIS を超えて時間を進める
    clock.advanceSeconds(3_600 + 120);

    // 別キーへのアクセスで間引きが走り、古いエントリが消える
    assertTrue(rateLimiter.tryAcquire("ip-new|AUTH", RULE));

    assertTrue(rateLimiter.size() <= 1);
  }
}
