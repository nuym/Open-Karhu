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
import me.liwk.karhu.util.discord.Webhook.EmbedObject.Author;
import me.liwk.karhu.util.discord.Webhook.EmbedObject.Footer;
import me.liwk.karhu.util.discord.Webhook.EmbedObject.Image;

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
      if (this.content == null && this.embeds.isEmpty()) {
         throw new IllegalArgumentException("Set content or add at least one EmbedObject");
      } else {
         Webhook.JSONObject json = new Webhook.JSONObject(null);
         json.put("content", this.content);
         json.put("username", this.username);
         json.put("avatar_url", this.avatarUrl);
         json.put("tts", this.tts);
         if (!this.embeds.isEmpty()) {
            List<Webhook.JSONObject> embedObjects = new ArrayList<>();

            for(Webhook.EmbedObject embed : this.embeds) {
               Webhook.JSONObject jsonEmbed = new Webhook.JSONObject(null);
               jsonEmbed.put("title", embed.getTitle());
               jsonEmbed.put("description", embed.getDescription());
               jsonEmbed.put("url", embed.getUrl());
               if (embed.getColor() != null) {
                  Color color = embed.getColor();
                  int rgb = color.getRed();
                  rgb = (rgb << 8) + color.getGreen();
                  rgb = (rgb << 8) + color.getBlue();
                  jsonEmbed.put("color", rgb);
               }

               Footer footer = embed.getFooter();
               Image image = embed.getImage();
               Webhook.EmbedObject.Thumbnail thumbnail = embed.getThumbnail();
               Author author = embed.getAuthor();
               List<Webhook.EmbedObject.Field> fields = embed.getFields();
               if (footer != null) {
                  Webhook.JSONObject jsonFooter = new Webhook.JSONObject(null);
                  jsonFooter.put("text", Footer.access$100(footer));
                  jsonFooter.put("icon_url", Footer.access$200(footer));
                  jsonEmbed.put("footer", jsonFooter);
               }

               if (image != null) {
                  Webhook.JSONObject jsonImage = new Webhook.JSONObject(null);
                  jsonImage.put("url", Image.access$300(image));
                  jsonEmbed.put("image", jsonImage);
               }

               if (thumbnail != null) {
                  Webhook.JSONObject jsonThumbnail = new Webhook.JSONObject(null);
                  jsonThumbnail.put("url", thumbnail.getUrl());
                  jsonEmbed.put("thumbnail", jsonThumbnail);
               }

               if (author != null) {
                  Webhook.JSONObject jsonAuthor = new Webhook.JSONObject(null);
                  jsonAuthor.put("name", Author.access$500(author));
                  jsonAuthor.put("url", Author.access$600(author));
                  jsonAuthor.put("icon_url", Author.access$700(author));
                  jsonEmbed.put("author", jsonAuthor);
               }

               List<Webhook.JSONObject> jsonFields = new ArrayList<>();

               for(Webhook.EmbedObject.Field field : fields) {
                  Webhook.JSONObject jsonField = new Webhook.JSONObject(null);
                  jsonField.put("name", field.getName());
                  jsonField.put("value", field.getValue());
                  jsonField.put("inline", field.isInline());
                  jsonFields.add(jsonField);
               }

               jsonEmbed.put("fields", jsonFields.toArray());
               embedObjects.add(jsonEmbed);
            }

            json.put("embeds", embedObjects.toArray());
         }

         URL url = new URL(this.url);
         HttpsURLConnection connection = (HttpsURLConnection)url.openConnection();
         connection.addRequestProperty("Content-Type", "application/json");
         connection.addRequestProperty("User-Agent", "DiscordHook");
         connection.setDoOutput(true);
         connection.setRequestMethod("POST");
         connection.connect();

         try (OutputStream os = connection.getOutputStream()) {
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
         }

         try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
               response.append(inputLine);
            }
         }
      }
   }

   public static class EmbedObject {
      private String title;
      private String description;
      private String url;
      private Color color;
      private Footer footer;
      private Webhook.EmbedObject.Thumbnail thumbnail;
      private Image image;
      private Author author;
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

      public Footer getFooter() {
         return this.footer;
      }

      public Webhook.EmbedObject.Thumbnail getThumbnail() {
         return this.thumbnail;
      }

      public Image getImage() {
         return this.image;
      }

      public Author getAuthor() {
         return this.author;
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

      public Webhook.EmbedObject setFooter(String text, String icon) {
         this.footer = new Footer(this, text, icon, null);
         return this;
      }

      public Webhook.EmbedObject setThumbnail(String url) {
         this.thumbnail = new Webhook.EmbedObject.Thumbnail(url, null);
         return this;
      }

      public Webhook.EmbedObject setImage(String url) {
         this.image = new Image(this, url, null);
         return this;
      }

      public Webhook.EmbedObject setAuthor(String name, String url, String icon) {
         this.author = new Author(this, name, url, icon, null);
         return this;
      }

      public Webhook.EmbedObject addField(String name, String value, boolean inline) {
         this.fields.add(new Webhook.EmbedObject.Field(name, value, inline, null));
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
