package QViewer.Models;

public enum FieldType {
	Int(1),Double(2),Text(4),
	DualInt(5),DualDouble(6),Unknown(-1);
	private int value;
	private FieldType(int val){
		this.value=val;
	}
	public int getValue(){
		return value;
	}
	
}
