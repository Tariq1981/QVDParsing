package QViewer.Models;

public class Lineage {
	public String Discriminator;
    public String Statement;

    public Lineage(String d, String s){
      this.Discriminator = d;
      this.Statement = s;
    }
}
