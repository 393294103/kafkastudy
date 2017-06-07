package kafkastudy.kafka.demo;

import java.util.Arrays;
import java.util.Properties;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.jdbc.core.JdbcTemplate;

public class ConsumerRunable implements Runnable{
	
	 /**
	  * topic
	  */
	 private final static String topic = "test4";
	 
	 private JdbcTemplate jdbcTemplate;
	 /**
	 * ip��ַ�Ͷ˿�
	 */
	 private final static String ipAndPort = "192.168.95.73:9092";
	 
	 private KafkaConsumer<String, String> consumer;
	
	
	 public ConsumerRunable() {
		//DataSource ds = new DriverManagerDataSource("jdbc:mysql://192.168.95.70:3306/chigoo", "root", "beyondsoft");
		 
		 // ��������Դ�����ӳأ�dbcp
		
	    BasicDataSource basicDataSource = new BasicDataSource();
	
	    // dbpcp����Դ�Ļ�������
	    basicDataSource.setDriverClassName("com.mysql.jdbc.Driver"); // ��������
	    basicDataSource.setUrl("jdbc:mysql://192.168.95.70:3306/chigoo");  // ���ݿ��
	    basicDataSource.setUsername("root");
	    basicDataSource.setPassword("beyondsoft");
		jdbcTemplate = new JdbcTemplate(basicDataSource);
        Properties props = new Properties();
        props.put("bootstrap.servers", ipAndPort);
        props.put("group.id", "abc");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
	}
	
	@Override
	public void run() {
		while(true){
			ConsumerRecords<String, String> records = consumer.poll(1000);
            //��consumer��ȡ�����ݣ��������ݿ���
            for (ConsumerRecord<String, String> record : records){
            	
            	String sql = "insert into sample(s_code,s_desc) values ('"+record.key()+"','"+record.value()+"')";
            	jdbcTemplate.execute(sql);
            	System.out.printf("offset = %d, key = %s, value = %s ,partition=%s,thread = %s\n,", record.offset(), record.key(), record.value(),record.partition(),Thread.currentThread());
            }
		}
	}
	

	public static void main(String[] args) {
		int i=0;
		//ʹ��50��consumer��������
		while(i<50){
			Thread t = new Thread(new ConsumerRunable());
			t.start();
			i++;
		}
	}
}
