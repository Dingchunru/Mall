package com.mall.common.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class JsonUtilsTest {

    @Test
    void testToJsonAndFromJson() {
        // 测试对象
        TestUser user = new TestUser();
        user.setId(1L);
        user.setName("张三");
        user.setAge(25);
        user.setCreateTime(LocalDateTime.now());

        // 对象转JSON
        String json = JsonUtils.toJson(user);
        assertThat(json).isNotNull();

        // JSON转对象
        TestUser parsedUser = JsonUtils.fromJson(json, TestUser.class);
        assertThat(parsedUser).isNotNull();
        assertThat(parsedUser.getId()).isEqualTo(user.getId());
        assertThat(parsedUser.getName()).isEqualTo(user.getName());
        assertThat(parsedUser.getAge()).isEqualTo(user.getAge());
    }

    @Test
    void testToList() {
        List<TestUser> userList = new ArrayList<>();
        TestUser user1 = new TestUser(1L, "张三", 25);
        TestUser user2 = new TestUser(2L, "李四", 30);
        userList.add(user1);
        userList.add(user2);

        String json = JsonUtils.toJson(userList);
        List<TestUser> parsedList = JsonUtils.toList(json, TestUser.class);

        assertThat(parsedList).hasSize(2);
        assertThat(parsedList.get(0).getName()).isEqualTo("张三");
        assertThat(parsedList.get(1).getName()).isEqualTo("李四");
    }

    @Test
    void testToMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 25);
        map.put("score", 95.5);

        String json = JsonUtils.toJson(map);
        Map<String, Object> parsedMap = JsonUtils.toMap(json);

        assertThat(parsedMap).isNotNull();
        assertThat(parsedMap.get("name")).isEqualTo("张三");
        assertThat(parsedMap.get("age")).isEqualTo(25);
        assertThat(parsedMap.get("score")).isEqualTo(95.5);
    }

    @Test
    void testToJsonWithTypeReference() {
        Map<String, List<TestUser>> data = new HashMap<>();
        data.put("users", Arrays.asList(
                new TestUser(1L, "张三", 25),
                new TestUser(2L, "李四", 30)
        ));

        String json = JsonUtils.toJson(data);
        Map<String, List<TestUser>> parsedData = JsonUtils.fromJson(json,
                new TypeReference<Map<String, List<TestUser>>>() {});

        assertThat(parsedData).isNotNull();
        assertThat(parsedData.get("users")).hasSize(2);
    }

    @Test
    void testIsJson() {
        String validJson = "{\"name\":\"张三\",\"age\":25}";
        String invalidJson = "not a json";

        assertThat(JsonUtils.isJson(validJson)).isTrue();
        assertThat(JsonUtils.isJson(invalidJson)).isFalse();
    }

    @Test
    void testFormatJson() {
        String json = "{\"name\":\"张三\",\"age\":25}";
        String formattedJson = JsonUtils.formatJson(json);

        assertThat(formattedJson).contains("\n");
        assertThat(formattedJson).contains("  ");
    }

    // 测试用的内部类
    static class TestUser {
        private Long id;
        private String name;
        private Integer age;
        private LocalDateTime createTime;

        public TestUser() {}

        public TestUser(Long id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }

        // getters and setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    }
}