package cross_game.x_o_game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;


public class Map extends JPanel {

    static final int GAME_MODE_HUMAN_VS_AI = 0;
    static final int GAME_MODE_HUMAN_VS_HUMAN = 1;

    private static final int EMPTY_DOT = 0;
    private static final int HUMAN_DOT = 1;
    private static final int AI_DOT = 2;

    private static final int DRAW = 0; //состояние на момент геймовер ничья
    private static final int HUMAN_WIN = 1;
    private static final int AI_WIN = 2;

    private static final int DOTS_MARGIN = 4;      //Отступ для рисования круга внутри ячейки

    private static final String DRAW_MSG = "Ничья!";
    private static final String HUMAN_WIN_MSG = "Выиграл человек!";
    private static final String AI_WIN_MSG = "Выиграл компьютер!";

    private final Random rnd = new Random();
    private boolean initialized; //отображается активность map
    private int[][] field;
    private int field_size_x;
    private int field_size_y;
    private int win_len;
    private int cell_width;
    private int cell_height;
    private boolean game_over;  //флаг который показывает окончена игра или нет
    private int game_over_state; //состояние игры после геймвоер
    private final Font font = new Font("Times new roman", Font.BOLD, 48);


    public Map() {
        setBackground(Color.white);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                update(e);

            }
        });
    }


    private void update(MouseEvent e) {
        if (game_over || !initialized) return;
        int cell_x = e.getX() / cell_width;
        int cell_y = e.getY() / cell_height;
        if (!isValidCell(cell_x, cell_y) || !isEmptyCell(cell_x, cell_y)) return;
        field[cell_y][cell_x] = HUMAN_DOT;
        repaint();
        if (checkWin(HUMAN_DOT)){
            game_over_state = HUMAN_WIN;
            game_over = true;
            return;
        }
        if (isMapFull()) {
            game_over_state = DRAW;
            game_over = true;
            return;
        }
        aiTurn();
        repaint();
        if (checkWin(AI_DOT)) {
            game_over_state = AI_WIN;
            game_over = true;
            return;
        }
        if (isMapFull()) {
            game_over_state = DRAW;
            game_over = true;
        }
    }

    void startNewGame(int mode, int field_size_x, int field_size_y, int win_len) {
        this.field_size_x = field_size_x;
        this.field_size_y = field_size_y;
        this.win_len = win_len;
        field = new int[field_size_y][field_size_x];
        game_over = false;
        initialized = true;
        repaint();


    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g);

    }


    private void render(Graphics g) {
        if (!initialized) return;
        int panel_width = getWidth();
        int panel_height = getHeight();
        cell_width = panel_width / field_size_x;
        cell_height = panel_height / field_size_y;
        for (int i = 0; i <= field_size_y; i++) {
            int y = i * cell_height;
            g.drawLine(0, y, panel_width, y);
        }
        for (int i = 0; i <= field_size_x; i++) {
            int x = i * cell_width;
            g.drawLine(x, 0, x, panel_height);
        }
        for (int i = 0; i < field_size_y; i++) {
            for (int j = 0; j < field_size_x; j++) {
                if (isEmptyCell(j, i)) continue;
                if (field[i][j] == HUMAN_DOT){
                    g.setColor(Color.BLUE);     //синим цветом рисуются фишки человека
                } else if (field [i][j] == AI_DOT){
                    g.setColor(Color.RED);
                } else {
                    throw new RuntimeException("Некорректное значение поля " + field[i][j]);
                }
                g.fillOval(j*cell_width + DOTS_MARGIN, i*cell_height + DOTS_MARGIN,
                        cell_width - 2 * DOTS_MARGIN, cell_height -  2 * DOTS_MARGIN);
            }
        }
        if (game_over)showGameOver(g);
    }

    private void showGameOver (Graphics g){
            g.setColor(Color.DARK_GRAY);
            g.fillRect(0, 200, getWidth(), 70);
            g.setColor(Color.YELLOW);
            g.setFont(font);
            switch (game_over_state) {
                case DRAW:
                    g.drawString(DRAW_MSG, 180, getHeight() / 2);
                    break;
                case HUMAN_WIN:
                    g.drawString(HUMAN_WIN_MSG, 70, getHeight() / 2);
                    break;
                case AI_WIN:
                    g.drawString(AI_WIN_MSG, 20, getHeight() / 2);
                    break;
                default:
                    throw new RuntimeException("Неизвестный game_over_state");
        }
    }


    private void aiTurn() {
        if(detectAIWinCell()) return;
        if(detectHumanWinCell()) return;
        int x, y;
        do {
            x = rnd.nextInt(field_size_x);
            y = rnd.nextInt(field_size_y);
        } while (!isEmptyCell(x, y));
        field[y][x] = AI_DOT;
    }

    private boolean detectAIWinCell(){
        for (int i = 0; i < field_size_y; i++) {
            for (int j = 0; j < field_size_x; j++) {
                if(isEmptyCell(j, i)) {
                    field[i][j] = AI_DOT;
                    if (checkWin(AI_DOT)) return true;
                    field[i][j] = EMPTY_DOT;
                }
            }
        }
        return false;
    }

    private boolean detectHumanWinCell(){
        for (int i = 0; i < field_size_y; i++) {
            for (int j = 0; j < field_size_x; j++) {
                if(isEmptyCell(j, i)) {
                    field[i][j] = HUMAN_DOT;
                    if (checkWin(HUMAN_DOT)) {
                        field[i][j] = AI_DOT;
                        return true;
                    }
                    field[i][j] = EMPTY_DOT;
                }
            }
        }
        return false;
    }

    private  boolean checkWin(int dot) {
        for(int i=0; i<field_size_x; i++){
            for (int j = 0; j<field_size_x; j++) {
                if(checkLine(i, j, 1, 0, win_len, dot)) return true;
                if(checkLine(i, j, 1, 1, win_len, dot)) return true;
                if(checkLine(i, j, 0, 1, win_len, dot)) return true;
                if(checkLine(i, j, 1, -1, win_len, dot)) return true;
            }
        }
        return false;
    }

    private  boolean checkLine(int x, int y, int vx, int vy, int len, int dot) {
        final int far_x = x + (len - 1) * vx;
        final int far_y = y + (len - 1) * vy;
        if (!isValidCell(far_x, far_y)) return false;
        for (int i = 0; i < len; i++) {
            if (field[y + i * vy][x + i * vx] != dot) return false;
        }
        return true;
    }

    private boolean isMapFull() {
        for (int i = 0; i < field_size_y; i++) {
            for (int j = 0; j < field_size_x; j++) {
                if (field[i][j] == EMPTY_DOT) return false;
            }
        }
        return true;
    }

    private boolean isValidCell(int x, int y) { return x >= 0 && x < field_size_x && y >= 0 && y < field_size_y; }

    private boolean isEmptyCell(int x, int y) { return field[y][x] == EMPTY_DOT; }
}