package com.web.gallery.event;

import static org.junit.jupiter.api.Assertions.*;

import com.web.gallery.domain.account.AccountNo;
import com.web.gallery.domain.photo.PhotoNo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class PhotoEventListenerTest {
  @InjectMocks private PhotoEventListener photoEventListener;

  @Test
  @DisplayName("正常系：PhotoRegisteredEventを受け取っても例外が発生しないこと")
  void handle_photoRegisteredEvent_success() {
    PhotoRegisteredEvent event = new PhotoRegisteredEvent(new AccountNo(1L), new PhotoNo(1L));
    assertDoesNotThrow(() -> photoEventListener.handle(event));
  }

  @Test
  @DisplayName("正常系：PhotoUpdatedEventを受け取っても例外が発生しないこと")
  void handle_photoUpdatedEvent_success() {
    PhotoUpdatedEvent event = new PhotoUpdatedEvent(new AccountNo(1L), new PhotoNo(1L));
    assertDoesNotThrow(() -> photoEventListener.handle(event));
  }

  @Test
  @DisplayName("正常系：PhotoDeletedEventを受け取っても例外が発生しないこと")
  void handle_photoDeletedEvent_success() {
    PhotoDeletedEvent event = new PhotoDeletedEvent(new AccountNo(1L), new PhotoNo(1L));
    assertDoesNotThrow(() -> photoEventListener.handle(event));
  }
}
