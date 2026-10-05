package com.huahua.huaaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;



public class FileBasedChatMemory implements ChatMemory {

    private final String BASE_DIR;
    private static final Kryo kryo = new Kryo();

    static {
        // 不用先注册什么类，遇到什么类就直接处理什么类
        kryo.setRegistrationRequired(false);
        //允许 Kryo 在反序列化（从文件读回对象）时，绕过构造函数直接创建对象
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String baseDir) {
        this.BASE_DIR = baseDir;
        File baseDirFile = new File(baseDir);
        if (!baseDirFile.exists()) {
            baseDirFile.mkdirs();
        }
    }
    @Override
    public void add(String conversationId, Message message) {
        ChatMemory.super.add(conversationId, message);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        //1.根据 id 获取消息列表
        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        //2.追加消息
        conversationMessages.addAll(messages);
        //3.将整个更新完后的完整列表序列化
        saveConversation(conversationId, conversationMessages);
    }


    @Override
    public List<Message> get(String conversationId) {
        //根据id拿到反序列化（kryo把文件序列化为message）的消息列表
       List<Message> allMessages = getOrCreateConversation(conversationId);
       return  allMessages.stream()
               .toList();
    }

    /**
     * 仅删除指定对话 ID 对应的文件
     * */
    @Override
    public void clear(String conversationId) {
        File file=getConversationFile(conversationId);
        if (file .exists()) {
            file.delete();
        }
    }

    /**
     * 拿到消息列表
     * 将文件还原为 messages 对象
     * */
    private List<Message> getOrCreateConversation(String conversationId) {
        //1.定位文件
       File file = getConversationFile(conversationId);
       List<Message> messages = new ArrayList<>();

       //2.打开文件流 （把文件变成一个数据流 input ，让 Kryo能够读取里面的字节）
       if (file.exists()) {
           try (Input input=new Input(new FileInputStream(file))) {
               //3.Kryo 读取文件里的字节流，将其还原（反序列化）成一个 List<Message> 对象
               messages = kryo.readObject(input, ArrayList.class);
           }catch (Exception e){
               e.printStackTrace();
           }
       }
       return messages;
    }

    /**
     * 将消息列表序列化回文件
     * */
    private void saveConversation(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);
        try (Output output=new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据 id 读取文件 没有则创建
     * */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR , conversationId+".kryo");
    }
}
