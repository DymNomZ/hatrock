package src;

import classes.dialogues.Dialogues;
import classes.entities.*;
import classes.map.Tile;
import interfaces.CollisionHandler;
import java.awt.Graphics;
import java.util.ArrayList;

public class MapConstructor {

    MapLoader map_loader;

    ArrayList<Tile> tile_data;
    Tile tiles[][];
    int door_coords[] = {47, 18, 48, 18, 84, 17, 85, 17, 153, 20, 154, 20, 153, 21, 154, 21, 195, 30, 196, 30, 195, 31, 196, 31};
    int map_indexes[][];
    int map_height, map_length, door_index = 0;

    public CameraEntity camera;
    int recent_x = 0, recent_y = 0;
    
    String map_path;

    private final Tile DEFAULT_TILE;

    public MapConstructor(String path, int screenWidth, int screenHeight){

        DEFAULT_TILE = new Tile("void.png", "void", 0, false, false);

        map_path = path;

        map_loader = new MapLoader();
        map_loader.loadMapData(map_path);

        tile_data = map_loader.getTiles();
        map_indexes = map_loader.getMapIndexes();

        map_height = map_indexes.length;
        map_length = map_indexes[0].length;

        loadMapTiles();

        //locate door coords, debug purposes only
        locateDoors();

        // System.out.println("Door coords: " + door_coords);

        camera = new CameraEntity(screenWidth, screenHeight);
    }

    public int[][] getMap_indexes(){
        return map_indexes;
    }

    public int getMap_height(){
        return map_height;
    }

    public int getMap_length(){
        return map_length;
    }

    private void locateDoors(){
        for(int i = 0; i < tiles.length; i++){
            for(int j = 0; j < tiles[0].length; j++){
                if(Utils.doorChecker(tiles[i][j].getName())){
                    System.out.print(j + " " + i + " ");
                }
            }
        }
        System.out.println();
    }

    public void loadMapTiles(){

        map_length = map_indexes[0].length;
        map_height = map_indexes.length;

        this.tiles = new Tile[map_height][map_length];
        for(int i = 0; i < map_height; i++){
            for(int j = 0; j < map_length; j++){
                
                //check which tile in tile data matches index
                if(map_indexes[i][j] != 0){
                    for(Tile t : tile_data){
                        if(t.index == map_indexes[i][j]){
                            this.tiles[i][j] = t;
                            break;
                        }
                    }
                }else{
                    this.tiles[i][j] = DEFAULT_TILE;
                }
                    
            }
        }
    }

    private int getVerifiedPosition(int position, int oppositePosition, int size, int oppositeSize, int deltaPosition, boolean isXDimension) {
        /* "Quick" summary of this bulshet below
         * 
         * Collisions are check sequentially... x dimension first and then y dimension
         * As trying to move an entity diagonally will have a high chance of having this entity
         * unable to check tiles in corners
         * 
         * This function will also check for map border collisions... If u want to move an entity
         * without the restrictions of the collisions, use the PanelEntity.moveAbsolute or change
         * the x and y coordinates directly without setting the deltaX and deltaY variables.
         * Collision will only verify those in deltaX and deltaY
         * 
         * Two major variables are present in both dimensions namely: pixelsOnTile and offTile
         * 
         * pixelsOnTile: represents the number of pixels of the entity's current position to the
         *               base point (0, 0) position of the current tile. This can also be known
         *               as the offset of the entity from current tile's top left corner
         *          
         *               Example each tile of the map have 32x32 dimension, and the position of the entity
         *               in relation to the pixels is (64, 33). The value for pixelsOnTile on both
         *               dimensions is (0, 1).
         * 
         * offTile:      This is the main factor of detecting whether we should check the next tile or not.
         *               This is the sum of pixelsOnTile and the speed/movement of an entity. Why you ask?
         *               If the value of this variable a negative value, this represents that we must check
         *               the tile before it. If the value is greater than the tile width, then we must check
         *               the tile after entity or the current tile. Any value that does not apply to the
         *               conditions above are ignored as they are expected to not collide a new tile.
         *               However, if the position of the entity is sitting at the base point (0, 0) of the
         *               current tile and this variable is greater than 0, we must still check the tile next
         *               of the current tile.
         * 
         * 
         * Since the tiles we used are placed in a 2D array, variables offXTile and offYTile are utilize to
         * get the index of the tile to check. In any possibility that an Entity is moving between two tiles,
         * we must check two tiles opposite to the current dimension we are calculating. If the first tile
         * is a solid, there is no need to check for the second tile since it is already given the entity
         * must not move there.
         * "two tiles opposite to the current dimension" means that if we are currently checking for x dimension,
         * we must check for tile[offYTile][offXTile] and tile[offYTile + 1][offXTile]. To check whether we have
         * to check two tiles in this situation, y % Panel.TILE_SIZE > 0 will tell us just that... also opposite to the
         * current dimension
         *
         *
         * [NOTE] not updated
         * - Lil Z
         */

        int maxSpeedPerLoop = GamePanel.TILE_SIZE;
        if (deltaPosition < 0) {
            maxSpeedPerLoop *= -1;
        }
        int maxPixelOnMap = (isXDimension ? map_length : map_height) * GamePanel.TILE_SIZE;
        int offPixelOnTile = GamePanel.TILE_SIZE;
        if (size % GamePanel.TILE_SIZE == 0) {
            if (size > GamePanel.TILE_SIZE) {
                offPixelOnTile -= GamePanel.TILE_SIZE;
            } else {
                offPixelOnTile -= size;
            }
        } else {
            offPixelOnTile -= size % GamePanel.TILE_SIZE;
        }

        int oppositeOffTile = oppositeSize % GamePanel.TILE_SIZE;
        boolean isBetween = oppositePosition % GamePanel.TILE_SIZE > (oppositeOffTile == 0 ? 0 : GamePanel.TILE_SIZE - oppositeOffTile);
        int loopN = oppositeSize / GamePanel.TILE_SIZE;
        if (oppositeOffTile > 0) loopN++;
        if (isBetween) loopN++;

        int initOffXTile = 0;
        int initOffYTile = 0;
        int nextOffXTile = 0;
        int nextOffYTile = 0;

        if (isXDimension) {
            initOffYTile = oppositePosition / GamePanel.TILE_SIZE;
            nextOffYTile = 1;
        } else {
            initOffXTile = oppositePosition / GamePanel.TILE_SIZE;
            nextOffXTile = 1;
        }

        while (deltaPosition != 0) {
            int deltaTemp = deltaPosition % maxSpeedPerLoop;
            if (deltaTemp == 0) {
                deltaTemp = maxSpeedPerLoop / 2;
            }
            deltaPosition -= deltaTemp;

            int pixelsOnTile = position % GamePanel.TILE_SIZE;
            int offTile = pixelsOnTile + deltaTemp;

            if (position + deltaTemp < 0) {
                position = 0;
                deltaPosition = 0;
            } else if (position + size + deltaTemp >= maxPixelOnMap) {
                position = maxPixelOnMap - size;
                deltaPosition = 0;
            } else if (offTile >= 0 && ((pixelsOnTile < offPixelOnTile && offTile <= offPixelOnTile) || (pixelsOnTile > offPixelOnTile && offTile <= GamePanel.TILE_SIZE + offPixelOnTile))) {
                position += deltaTemp;
            } else {
                int offXTile = initOffXTile;
                int offYTile = initOffYTile;

                if (offTile >= 0) {
                    if (isXDimension) {
                        offXTile = (position + deltaTemp + size) / GamePanel.TILE_SIZE;
                    } else {
                        offYTile = (position + deltaTemp + size) / GamePanel.TILE_SIZE;
                    }
                } else {
                    if (isXDimension) {
                        offXTile = (position + deltaTemp) / GamePanel.TILE_SIZE;
                    } else {
                        offYTile = (position + deltaTemp) / GamePanel.TILE_SIZE;
                    }
                }

                position += deltaTemp;

                for (int i = loopN; i >= 1; i--) {
                        if (tiles[offYTile][offXTile].is_solid) {
                            if (offTile < 0) {
                                position -= offTile;
                            } else {
                                if (pixelsOnTile == offPixelOnTile) {
                                    position -= deltaTemp;
                                } else if (pixelsOnTile > offPixelOnTile) {
                                    position -= offTile - offPixelOnTile - GamePanel.TILE_SIZE;
                                } else {
                                    position -= offTile - offPixelOnTile;
                                }
                            }
                            deltaPosition = 0;
                            break;
                        }

                    offYTile += nextOffYTile;
                    offXTile += nextOffXTile;
                }
            }
        }

        return position;
    }

    public void verifyEntityPosition(PanelEntity e){
        boolean isCollided = false;

        int tempPos = e.x;
        e.x = getVerifiedPosition(e.x, e.y, e.width, e.height, e.deltaX, true);
        if (e instanceof CollisionHandler ent && e.x - e.deltaX != tempPos) {
            if (e.x - e.deltaX < tempPos) {
                ent.onRightCollision();
            } else {
                ent.onLeftCollision();
            }
            isCollided = true;
        }

        if(e instanceof Enemy && ((Enemy) e).getMovement_internal_cooldown() == 0 && ((Enemy) e).isGoingToMove())
            e.deltaX = 0;

        tempPos = e.y;
        e.y = getVerifiedPosition(e.y, e.x, e.height, e.width, e.deltaY, false);
        if (e instanceof CollisionHandler ent && e.y - e.deltaY != tempPos) {
            if (e.y - e.deltaY < tempPos) {
                ent.onBottomCollision();
            } else {
                ent.onTopCollision();
            }
            isCollided = true;
        }
        if(e instanceof Enemy && ((Enemy) e).getMovement_internal_cooldown() == 0 && ((Enemy) e).isGoingToMove())
            e.deltaY = 0;

        if (e instanceof CollisionHandler ent && isCollided) {
            ent.onCollision();
        }

    }

    // Note: x and y not centered
    public void view(int x, int y) {
        camera.x = Math.max(x, 0);
        camera.x = Math.min(camera.x, (map_length * GamePanel.TILE_SIZE) - camera.width);

        camera.y = Math.max(y, 0);
        camera.y = Math.min(camera.y, (map_height * GamePanel.TILE_SIZE) - camera.height);
    }

    // Note: center to entity
    public void view(PanelEntity e) {
        camera.x = Math.max(0, e.x - ((camera.width / 2) - (e.width / 2) - 2));
        camera.x = Math.min(camera.x, (map_length * GamePanel.TILE_SIZE) - camera.width);
    
        camera.y = Math.max(0, e.y - ((camera.height / 2) - (e.height / 2)));
        camera.y = Math.min(camera.y, (map_height * GamePanel.TILE_SIZE) - camera.height);
    }

    public void displayEntity(PanelEntity e, Graphics g) {
        if (e.x >= camera.x - e.width && e.x <= camera.x + camera.width) {
            if (e.y >= camera.y - e.height && e.y <= camera.y + camera.height) {
                e.display(g, camera);
            }
        }
    }

    public void displayTiles(Graphics g) {
        int leftStart = Math.max((camera.x / GamePanel.TILE_SIZE) - 1, 0);
        int topStart = Math.max((camera.y / GamePanel.TILE_SIZE) - 1, 0);
        int rightEnd = Math.min(((camera.x + camera.width) / GamePanel.TILE_SIZE) + 1, map_length);
        int bottomEnd = Math.min(((camera.y + camera.height) / GamePanel.TILE_SIZE) + 1, map_height);

        while (topStart < bottomEnd) {
            for (int i = leftStart; i < rightEnd; i++) {
                int tileX = (i * GamePanel.TILE_SIZE) - camera.x;
                int tileY = (topStart * GamePanel.TILE_SIZE) - camera.y;

                if(PlayerData.kill_count >= PlayerData.required_kills && Dialogues.PHASE_STATE != 6){
                    //trigger cleared stage dialogue in Dialogues class
                    PlayerData.trigger_spawning = false;
                    if(Dialogues.PHASE_STATE < 5) PlayerData.cleared_stage = true;
                    handleDoors();
                }

                if(PlayerData.boss_ready) handleDoors();

                tiles[topStart][i].image.display(g, tileX, tileY, GamePanel.TILE_SIZE, GamePanel.TILE_SIZE);
            }
            topStart++;
        }
    }

    //door system
    public void handleDoors(){
        int x = 0, y = 0;
        
        if(door_index < door_coords.length){
            x = door_coords[door_index];
            y = door_coords[door_index+1];
        }

        if(Dialogues.PHASE_STATE < 4){
            replaceTile(y, x, Utils.doors_opened[0], false, false);
            replaceTile(y, x+1, Utils.doors_opened[1], false, false);
            door_index += 4;
        }
        else if(Dialogues.PHASE_STATE >= 4 && Dialogues.PHASE_STATE < 7){
            replaceTile(y, x, Utils.doors_opened[2], false, false);
            replaceTile(y, x+1, Utils.doors_opened[3], false, false);
            replaceTile(y+1, x, Utils.doors_opened[4], false, false);
            replaceTile(y+1, x+1, Utils.doors_opened[5], false, false);
            if(door_index < door_coords.length) door_index += 8;
        }

        PlayerData.kill_count = 0;
        if(PlayerData.check_pointer < PlayerData.CHECK_LIMIT){
            //update check pointer and move to next state
            PlayerData.check_pointer++;
            Dialogues.PHASE_STATE++;
        }
        PlayerData.boss_ready = false;
        
    }

    private void replaceTile(int j, int i, String path, boolean solid_state, boolean animated_state){

        int idx = tiles[j][i].index;
        String name = path.substring(0, path.lastIndexOf(".")+1);

        tiles[j][i] = new Tile(path, name, idx, solid_state, animated_state);
    }

}
