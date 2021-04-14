class Program{
    public static void main(String[] args){
        State state = State.IDLE___AAAAAAFwp3gktl12yTE; 
        String name = state.toString().split("___")[0]; 
        System.out.println(name);
    }


}

//  enum State {
//     PEPSI("Pepsi___77"), 
//     COKE("Coca Cola"), 
//     SPRITE("Sprite");
    
//     private String str;
//     private State(String str) {
//         this.str = str;
//     }
   
//     @Override
//     public String toString(){
//         return str;
//     }
// }

enum State {
	IDLE___AAAAAAFwp3gktl12yTE("IDLE___AAAAAAFwp3gktl12yTE"),
	FORWARD___AAAAAAFwp3hHMV2cNbw("FORWARD___AAAAAAFwp3hHMV2cNbw"),
	BACKWARD___AAAAAAFwp3hz6l3CpOY("BACKWARD___AAAAAAFwp3hz6l3CpOY"),
	ROTATE_LEFT___AAAAAAFwp3iJR13o670("ROTATE_LEFT___AAAAAAFwp3iJR13o670"),
	ROTATE_RIGHT___AAAAAAFwp3ipDl4OxBw("ROTATE_RIGHT___AAAAAAFwp3ipDl4OxBw"),
	ROTATE_LEFT___AAAAAAFx9a6FYFxmG0("ROTATE_LEFT___AAAAAAFx9a6FYFxmG0");

	private String str;
	private State(String str) {
		this.str = str;
	}
	@Override
	public String toString() {
		return str;
	}
}