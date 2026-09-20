package com.tickethub.domain.core.spot;

/**
 * A spot together with the denormalized ownership links kept by persistence
 * (see {@code SpotDocument}): the show and section it was generated for.
 * Used by ticket validation to prove the QR code really belongs to the
 * show/section presented at the door.
 */
public record SpotPlacement(Spot spot, String showId, String sectionId) {
}
