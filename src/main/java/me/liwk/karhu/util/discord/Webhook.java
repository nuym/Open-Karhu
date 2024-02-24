package me.liwk.karhu.util.discord;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;

public class Webhook {
   private final String url;
   private String content;
   private String username;
   private String avatarUrl;
   private boolean tts;
   private List<Webhook.EmbedObject> embeds = new ArrayList<>();

   public Webhook(String url) {
      this.url = url;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public void setUsername(String username) {
      this.username = username;
   }

   public void setAvatarUrl(String avatarUrl) {
      this.avatarUrl = avatarUrl;
   }

   public void setTts(boolean tts) {
      this.tts = tts;
   }

   public void addEmbed(Webhook.EmbedObject embed) {
      this.embeds.add(embed);
   }

   public void execute() throws IOException {

   }

   public static class EmbedObject {
      private String title;
      private String description;
      private String url;
      private Color color;

      private Webhook.EmbedObject.Thumbnail thumbnail;
      private List<Webhook.EmbedObject.Field> fields = new ArrayList<>();

      public String getTitle() {
         return this.title;
      }

      public String getDescription() {
         return this.description;
      }

      public String getUrl() {
         return this.url;
      }

      public Color getColor() {
         return this.color;
      }


      public Webhook.EmbedObject.Thumbnail getThumbnail() {
         return this.thumbnail;
      }


      public List<Webhook.EmbedObject.Field> getFields() {
         return this.fields;
      }

      public Webhook.EmbedObject setTitle(String title) {
         this.title = title;
         return this;
      }

      public Webhook.EmbedObject setDescription(String description) {
         this.description = description;
         return this;
      }

      public Webhook.EmbedObject setUrl(String url) {
         this.url = url;
         return this;
      }

      public Webhook.EmbedObject setColor(Color color) {
         this.color = color;
         return this;
      }




      private class Field {
         private String name;
         private String value;
         private boolean inline;

         private Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
         }

         private String getName() {
            return this.name;
         }

         private String getValue() {
            return this.value;
         }

         private boolean isInline() {
            return this.inline;
         }
      }

      private class Thumbnail {
         private String url;

         private Thumbnail(String url) {
            this.url = url;
         }

         private String getUrl() {
            return this.url;
         }
      }
   }

   private class JSONObject {
      private final HashMap<String, Object> map = new HashMap<>();

      private JSONObject() {
      }

      void put(String key, Object value) {
         if (value != null) {
            this.map.put(key, value);
         }
      }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         Set<Entry<String, Object>> entrySet = this.map.entrySet();
         builder.append("{");
         int i = 0;

         for(Entry<String, Object> entry : entrySet) {
            Object val = entry.getValue();
            builder.append(this.quote(entry.getKey())).append(":");
            if (val instanceof String) {
               builder.append(this.quote(String.valueOf(val)));
            } else if (val instanceof Integer) {
               builder.append(Integer.valueOf(String.valueOf(val)));
            } else if (val instanceof Boolean) {
               builder.append(val);
            } else if (val instanceof Webhook.JSONObject) {
               builder.append(val.toString());
            } else if (val.getClass().isArray()) {
               builder.append("[");
               int len = Array.getLength(val);

               for(int j = 0; j < len; ++j) {
                  builder.append(Array.get(val, j).toString()).append(j != len - 1 ? "," : "");
               }

               builder.append("]");
            }

            ++i;
            builder.append(i == entrySet.size() ? "}" : ",");
         }

         return builder.toString();
      }

      private String quote(String string) {
         return "\"" + string + "\"";
      }
   }
}
