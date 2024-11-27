package com.rxlogix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.Gson;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataTableSearchRequest {

    private DataTableSearchParam searchParam;

    public int pageNumber() {
        return searchParam.getPageNumber();
    }

    public int pageSize() {
        return searchParam.getPageSize();
    }

    public int draw() {
        return searchParam.getDraw();
    }

    public String orderBy() {
        return searchParam.orderBy();
    }

    public Direction orderDir() {
        return Direction.fromString(searchParam.orderDir());
    }

    public void setArgs(String args) {
        searchParam = fromJSON(args, DataTableSearchParam.class);
    }

    private static class DataTableSearchParam {
        private int start;
        private int length;
        private int draw;
        private List<Columns> columns;
        private List<Order> order;
        private Search search;

        public void setStart(int start) {
            this.start = start;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getPageNumber() {
            return (start / length + 1) - 1;
        }

        public int getPageSize() {
            return length == -1 ? Integer.MAX_VALUE : length;
        }

        public int getDraw() {
            return draw;
        }

        public void setDraw(int draw) {
            this.draw = draw;
        }

        public String orderBy() {
            return defaultIfBlank(this.columns.get(order.get(0).column).name, "id");
        }

        public String orderDir() {
            return order.get(0).dir;
        }

        public void setColumns(List<Columns> columns) {
            this.columns = columns;
        }

        public void setOrder(List<Order> order) {
            this.order = order;
        }

        public void setSearch(Search search) {
            this.search = search;
        }
    }

    private static class Columns {
        private String data;
        private String name;
        private boolean orderable;
        private Search search;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOrderable() {
            return orderable;
        }

        public void setOrderable(boolean orderable) {
            this.orderable = orderable;
        }

        public void setSearch(Search search) {
            this.search = search;
        }

    }

    private static class Order {
        private int column;
        private String dir;

        public int getColumn() {
            return column;
        }

        public void setColumn(final int column) {
            this.column = column;
        }

        public String getDir() {
            return dir;
        }

        public void setDir(final String dir) {
            this.dir = dir;
        }
    }

    private static class Search {
        private String value;
        private boolean regex;
        List<String> fixed;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public boolean isRegex() {
            return regex;
        }

        public void setRegex(boolean regex) {
            this.regex = regex;
        }
        public List<String> getFixed() {
            return fixed;
        }
        public void setFixed(List<String> fixed) {
            this.fixed = fixed;
        }
    }

    public static <T> T fromJSON(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }
}

