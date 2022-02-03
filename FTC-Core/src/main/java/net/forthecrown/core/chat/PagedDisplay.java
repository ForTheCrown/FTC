package net.forthecrown.core.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.List;
import java.util.function.Supplier;

public interface PagedDisplay {
    static  <T> Component create(int page, int pageSize,
                                 List<T> list,
                                 EntryProvider<T> entryProvider, Supplier<Component> header, FooterProvider footer
    ) {
        TextComponent.Builder builder = Component.text()
                .append(header.get());

        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;
        int maxPage = (int) Math.ceil((double) list.size() / (double) pageSize);

        for (int i = startIndex; i < endIndex; i++) {
            if(i >= list.size()) break;

            T entry = list.get(i);

            builder
                    .append(Component.newline())
                    .append(entryProvider.entry(entry, i + 1));
        }

        return builder
                .append(Component.newline())
                .append(footer.createFooter(page + 1, endIndex >= list.size() - 1, page <= 0, maxPage))
                .build();
    }


    interface EntryProvider<T> {
        Component entry(T val, int index);
    }

    interface FooterProvider {
        Component createFooter(int currentPage, boolean lastPage, boolean firstPage, int maxPage);
    }
}
