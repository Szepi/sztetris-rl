/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tetrisengine;

import java.io.Serializable;

/**
 *
 * @author istvanszita
 */
public class TetrisAction implements Serializable {
    public int pos;
    public int rot;

    public TetrisAction(int pos, int rot)
    {
        this.pos = pos;
        this.rot = rot;
    }

    @Override
    public String toString()
    {
        return String.format("p:%d,r:%d", pos,rot);
    }
}
