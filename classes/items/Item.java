package classes.items;

public abstract class Item {
    private String name;
    private boolean is_stackable;
    private int quantity; // suggest to move to only CONSUMABLE since no reason to give weapons and stat
                          // mods quantity since limited to only 1? -Raymond
    private String item_type; // why isn't this visible? - FV
    // suggest to move to only WEAPONS since no reason to give consumables quality
    // level? -Raymond
    // da heck is quality level - Ervin //replied in gc -Raymond
    // I'm in favor of moving quality to the WEAPONS subclass - FV
    // Moved quality var to weapons class, if goods na we can delete the comments
    // for this to reduce clutter -Ervin
    private int id;

    public Item(String name, String item_type, int quantity, int id) // for consumables -Raymond
    {
        this.name = name;
        this.is_stackable = true;
        this.quantity = quantity;
        this.item_type = item_type;
        this.id = id;
    }

    public Item(String name, String item_type, int id) // for weapons and stat-modifiers -Raymond
    {
        this.name = name;
        this.is_stackable = false;
        this.quantity = 1;
        this.item_type = item_type;
        this.id = id;
    }

    // what's the idea for this? -Raymond
    // I don't know either, I'll change it to quantity += amount - FV
    protected void setQuantity(int amount) {
        quantity += amount;
    }

    public String getName() {
        return name;
    }

    public boolean isIs_stackable() {
        return is_stackable;
    }

    public int getId() {
        return id;
    }

    public int getQuantity() {
        return quantity; // may be used to check if item is 0 and therefore be removed
    }

    public void consumeItem() {
        /*
         * // consume method for consumables here, since quantity is private -FV
         * 
         * // suggest to move to CONSUMABLE class, same reason as quantity variable
         * -Raymond
         * 
         * // should be quality not quantity? also prolly no need for if statement since
         * if
         * // it reaches 0 it should automatically be removed from inventory -Raymond
         * 
         * // Yeah, I typed the wrong thing for the if-case (Resolved)
         * // Also I believe it's simpler to have a consumeItem and removeItem in this
         * class, so it
         * // may be used by other items. For example, if a weapon has durability.
         * // if weaponHP == 0, consumeItem(), since 1, quantity becomes 0, then remove
         * from inventory.
         * 
         * // if case to prevent quantity from going below 0; (I'm not sure how
         * necessary it is, but I have PTSD from CodeChum testcases)
         */

        if (quantity > 0) {
            quantity -= 1;
        }
        if (quantity == 0) {
            // removeItem(); - Who's willing to implement removeItem() function? - FV
        }

    }

}