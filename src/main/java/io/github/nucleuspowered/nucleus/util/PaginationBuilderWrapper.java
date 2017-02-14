/*
 * This file is part of Nucleus, licensed under the MIT License (MIT). See the LICENSE.txt file
 * at the root of this project for more details.
 */
package io.github.nucleuspowered.nucleus.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import io.github.nucleuspowered.nucleus.Util;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.List;
import java.util.ListIterator;

import javax.annotation.Nullable;

@NonnullByDefault
public class PaginationBuilderWrapper implements PaginationList.Builder {

    private final PaginationList.Builder builder;
    @Nullable private List<Text> texts = null;

    public PaginationBuilderWrapper(PaginationList.Builder builder) {
        this.builder = builder;
    }

    @Override public PaginationList.Builder contents(Iterable<Text> contents) {
        texts = Lists.newArrayList(contents);
        return this;
    }

    @Override public PaginationList.Builder contents(Text... contents) {
        texts = Lists.newArrayList(contents);
        return this;
    }

    @Override public PaginationList.Builder title(Text title) {
        return this.builder.title(title);
    }

    @Override public PaginationList.Builder header(@Nullable Text header) {
        return this.builder.header(header);
    }

    @Override public PaginationList.Builder footer(@Nullable Text footer) {
        return this.builder.footer(footer);
    }

    @Override public PaginationList.Builder padding(Text padding) {
        return this.builder.padding(padding);
    }

    @Override public PaginationList.Builder linesPerPage(int linesPerPage) {
        return this.builder.linesPerPage(linesPerPage);
    }

    @Override public PaginationList build() {
        Preconditions.checkNotNull(texts);
        ListIterator<Text> text = texts.listIterator();
        while (text.hasNext()) {
            Text t = text.next();
            if (t.toPlain().isEmpty()) {
                text.set(Util.NOT_EMPTY);
            }
        }

        return this.builder.contents(texts).build();
    }

    @Override public PaginationList.Builder from(PaginationList value) {
        return this.builder.from(value);
    }

    @Override public PaginationList.Builder reset() {
        return this.builder.reset();
    }
}
