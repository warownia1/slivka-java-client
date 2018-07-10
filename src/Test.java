import uk.ac.dundee.compbio.slivkaclient.FileHandler;
import uk.ac.dundee.compbio.slivkaclient.Service;
import uk.ac.dundee.compbio.slivkaclient.SlivkaClient;
import uk.ac.dundee.compbio.slivkaclient.TaskHandler;
import uk.ac.dundee.compbio.slivkaclient.form.Form;

public class Test {
	public static void main(String args[]) {		
		try {
			SlivkaClient client = new SlivkaClient("localhost", 8000);
			Service service = null;
			for (Service s : client.getServices()) {
				if (s.getName().equals("PyDummy")) {
					service = s;
				}
			}
			Form form = service.getForm();
			System.out.println(form.getFields());
			TaskHandler task = form
					.insert("message", "Hello World!!!")
					.insert("lunch", "chicken")
					.insert("wait", 3)
					.insert("number", 2)
					.insert("error", "Scary error message")
					.submit();
			while (!task.getStatus().isReady()) {
				Thread.sleep(100);
			}
			for (FileHandler file : task.getResult()) {
				System.out.format(
						"id: %1$s%ntitle: %2$s%ncontent:%n",
						file.getID(), file.getTitle()
						);
				file.writeTo(System.out);
				System.out.println("\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
