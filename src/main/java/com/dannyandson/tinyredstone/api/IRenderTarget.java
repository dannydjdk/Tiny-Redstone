package com.dannyandson.tinyredstone.api;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Abstraction over the two render layers a panel cell or cover draws into.
 * <p>
 * A cell needs "the solid consumer" OR "the translucent consumer" based on its alpha. This
 * keeps the panel-cell API decoupled from vanilla render-pipeline churn.
 */
public interface IRenderTarget {

    /**
     * Cutout/solid layer vertex consumer.
     * Formerly {@code buffer.getBuffer(Sheets.cutoutBlockSheet())}.
     */
    VertexConsumer solid();

    /**
     * Translucent layer vertex consumer.
     * Formerly {@code buffer.getBuffer(Sheets.translucentBlockSheet())}.
     */
    VertexConsumer translucent();
}