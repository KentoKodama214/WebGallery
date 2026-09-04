package com.web.gallery.helper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

/**
 * {@link ReauthenticationThrottle}のユニットテスト
 */
@ActiveProfiles("test")
public class ReauthenticationThrottleTest {

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

		void advanceMinutes(long minutes) {
			millis += minutes * 60_000L;
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

	@BeforeEach
	void setUp() {
		clock = new MutableClock(1_000_000_000_000L);
	}

	@Test
	@DisplayName("上限未満ではロックアウトしない")
	void not_locked_below_threshold() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(3, 15, clock);

		throttle.recordFailure(1L);
		throttle.recordFailure(1L);

		assertFalse(throttle.isLockedOut(1L));
	}

	@Test
	@DisplayName("上限に達するとロックアウトする")
	void locked_at_threshold() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(3, 15, clock);

		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);

		assertTrue(throttle.isLockedOut(1L));
		// 別アカウントには影響しない
		assertFalse(throttle.isLockedOut(2L));
	}

	@Test
	@DisplayName("ロックアウト時間が経過すると自動的に解除される")
	void unlocks_after_lockout_window() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(3, 15, clock);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		assertTrue(throttle.isLockedOut(1L));

		clock.advanceMinutes(15);

		assertFalse(throttle.isLockedOut(1L));
	}

	@Test
	@DisplayName("reset で失敗カウンタが消える")
	void reset_clears_counter() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(3, 15, clock);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		assertTrue(throttle.isLockedOut(1L));

		throttle.reset(1L);

		assertFalse(throttle.isLockedOut(1L));
	}

	@Test
	@DisplayName("max-failures が0以下なら常に無効（ロックアウトしない）")
	void disabled_when_max_failures_not_positive() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(0, 15, clock);

		for (int i = 0; i < 100; i++) {
			throttle.recordFailure(1L);
		}

		assertFalse(throttle.isLockedOut(1L));
	}

	@Test
	@DisplayName("前回失敗からロックアウト時間以上あくとカウントが振り出しに戻る")
	void counter_resets_after_idle_period() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(3, 15, clock);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);

		clock.advanceMinutes(15);
		throttle.recordFailure(1L);

		// 直前の2回は失効しているため、まだロックアウトされない
		assertFalse(throttle.isLockedOut(1L));
	}

	@Test
	@DisplayName("ロックアウト中の試行を記録するとロックアウトが延長される（スライディングウィンドウ）")
	void lockout_extends_on_attempt_during_lockout() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(3, 15, clock);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		assertTrue(throttle.isLockedOut(1L));

		// ロックアウト中に10分後、さらに試行して失敗を記録する
		clock.advanceMinutes(10);
		throttle.recordFailure(1L);

		// 最初の3回から15分経過してもなお、直近失敗から15分経っていないためロックアウト継続
		clock.advanceMinutes(6);
		assertTrue(throttle.isLockedOut(1L));

		// 直近失敗から15分経過すると解除される
		clock.advanceMinutes(9);
		assertFalse(throttle.isLockedOut(1L));
	}

	@Test
	@DisplayName("あるアカウントのロックアウトは他アカウントの失敗記録に影響されない")
	void lockout_is_isolated_per_account() {
		ReauthenticationThrottle throttle = new ReauthenticationThrottle(3, 15, clock);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		throttle.recordFailure(1L);
		assertTrue(throttle.isLockedOut(1L));

		for (int i = 0; i < 50; i++) {
			throttle.recordFailure(100L + i);
		}

		assertTrue(throttle.isLockedOut(1L));
	}
}
