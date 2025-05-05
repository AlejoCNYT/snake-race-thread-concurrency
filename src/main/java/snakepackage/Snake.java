package snakepackage;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Random;

import enums.Direction;
import enums.GridSize;

public class Snake extends Observable implements Runnable {

    private final int idt;
    private volatile Cell head;
    private Cell newCell;
    private final LinkedList<Cell> snakeBody = new LinkedList<>();
    private Cell start = null;

    private volatile boolean snakeEnd = false;

    private int direction = Direction.NO_DIRECTION;
    private final int INIT_SIZE = 3;

    private boolean hasTurbo = true;
    private int jumps = 0;
    private boolean isSelected = false;
    private int growing = 0;
    public boolean goal = false;

    private final Object lock = new Object();

    public Snake(int idt, Cell head, int direction) {
        this.idt = idt;
        this.direction = direction;
        generateSnake(head);
    }

    public boolean isSnakeEnd() {
        return snakeEnd;
    }

    private void generateSnake(Cell head) {
        start = head;
        this.head = head;
        snakeBody.add(head);
        growing = INIT_SIZE - 1;
        Board.gameboard[head.getX()][head.getY()].setFull(true);
    }

    @Override
    public void run() {
        while (!snakeEnd) {
            try {
                snakeCalc();

                setChanged();
                notifyObservers();

                if (hasTurbo) {
                    Thread.sleep(500 / 3);
                } else {
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                snakeEnd = true;
            } catch (Exception e) {
                System.err.println("[" + idt + "] Error in snake thread: " + e.getMessage());
                snakeEnd = true;
            }
        }

        if (head != null) {
            fixDirection(head);
        }
    }

    private void snakeCalc() {
        Cell currentHead;
        Cell nextCell;

        synchronized (lock) {
            currentHead = snakeBody.peekFirst();
            if (currentHead == null) {
                snakeEnd = true;
                return;
            }

            try {
                nextCell = changeDirection(currentHead);
                if (nextCell == null) {
                    snakeEnd = true;
                    return;
                }

                randomMovement(nextCell);

                checkIfFood(nextCell);
                checkIfJumpPad(nextCell);
                checkIfTurboBoost(nextCell);
                checkIfBarrier(nextCell);

                Board.gameboard[nextCell.getX()][nextCell.getY()].setFull(true);
                snakeBody.push(nextCell);
                this.head = nextCell;
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("[" + idt + "] Snake crashed into wall at " + currentHead);
                snakeEnd = true;
                return;
            }
        }

        if (growing <= 0) {
            Cell lastCell;
            synchronized (lock) {
                lastCell = snakeBody.peekLast();
                if (lastCell != null) {
                    snakeBody.remove(lastCell);
                    Board.gameboard[lastCell.getX()][lastCell.getY()].freeCell();
                }
            }
        } else {
            growing--;
        }
    }

    private void checkIfBarrier(Cell newCell) {
        if (newCell == null) return;

        try {
            if (Board.gameboard[newCell.getX()][newCell.getY()].isBarrier()) {
                System.out.println("[" + idt + "] CRASHED AGAINST BARRIER " + newCell);
                snakeEnd = true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[" + idt + "] Barrier check out of bounds at " + newCell);
            snakeEnd = true;
        }
    }

    private Cell fixDirection(Cell newCell) {
        if (newCell == null) return null;

        try {
            Cell fixedCell = newCell;

            if (direction == Direction.LEFT && newCell.getX() + 1 < GridSize.GRID_WIDTH) {
                fixedCell = Board.gameboard[newCell.getX() + 1][newCell.getY()];
            } else if (direction == Direction.RIGHT && newCell.getX() - 1 >= 0) {
                fixedCell = Board.gameboard[newCell.getX() - 1][newCell.getY()];
            } else if (direction == Direction.UP && newCell.getY() + 1 < GridSize.GRID_HEIGHT) {
                fixedCell = Board.gameboard[newCell.getX()][newCell.getY() + 1];
            } else if (direction == Direction.DOWN && newCell.getY() - 1 >= 0) {
                fixedCell = Board.gameboard[newCell.getX()][newCell.getY() - 1];
            }

            randomMovement(fixedCell);
            return fixedCell;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[" + idt + "] Fix direction out of bounds at " + newCell);
            return null;
        }
    }

    private boolean checkIfOwnBody(Cell newCell) {
        if (newCell == null) return false;

        synchronized (lock) {
            return snakeBody.stream()
                    .anyMatch(c -> c != null && newCell.getX() == c.getX() && newCell.getY() == c.getY());
        }
    }

    private void randomMovement(Cell newCell) {
        if (newCell == null) return;

        Random random = new Random();
        int tmp = random.nextInt(4) + 1;

        if (tmp == Direction.LEFT && direction != Direction.RIGHT) {
            direction = tmp;
        } else if (tmp == Direction.UP && direction != Direction.DOWN) {
            direction = tmp;
        } else if (tmp == Direction.DOWN && direction != Direction.UP) {
            direction = tmp;
        } else if (tmp == Direction.RIGHT && direction != Direction.LEFT) {
            direction = tmp;
        }
    }

    private void checkIfTurboBoost(Cell newCell) {
        if (newCell == null) return;

        try {
            if (Board.gameboard[newCell.getX()][newCell.getY()].isTurbo_boost()) {
                synchronized (Board.turbo_boosts) {
                    for (int i = 0; i < Board.NR_TURBO_BOOSTS; i++) {
                        if (Board.turbo_boosts[i] != null &&
                                Board.turbo_boosts[i].equals(newCell)) {
                            Board.turbo_boosts[i].setTurbo_boost(false);
                            Board.turbo_boosts[i] = new Cell(-5, -5);
                            hasTurbo = true;
                        }
                    }
                }
                System.out.println("[" + idt + "] GETTING TURBO BOOST " + newCell);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[" + idt + "] Turbo boost check out of bounds at " + newCell);
        }
    }

    private void checkIfJumpPad(Cell newCell) {
        if (newCell == null) return;

        try {
            if (Board.gameboard[newCell.getX()][newCell.getY()].isJump_pad()) {
                synchronized (Board.jump_pads) {
                    for (int i = 0; i < Board.NR_JUMP_PADS; i++) {
                        if (Board.jump_pads[i] != null &&
                                Board.jump_pads[i].equals(newCell)) {
                            Board.jump_pads[i].setJump_pad(false);
                            Board.jump_pads[i] = new Cell(-5, -5);
                            this.jumps++;
                        }
                    }
                }
                System.out.println("[" + idt + "] GETTING JUMP PAD " + newCell);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[" + idt + "] Jump pad check out of bounds at " + newCell);
        }
    }

    private void checkIfFood(Cell newCell) {
        if (newCell == null) return;

        try {
            if (Board.gameboard[newCell.getX()][newCell.getY()].isFood()) {
                synchronized (Board.food) {
                    growing += 3;
                    Random random = new Random();
                    int x = random.nextInt(GridSize.GRID_HEIGHT);
                    int y = random.nextInt(GridSize.GRID_WIDTH);

                    System.out.println("[" + idt + "] EATING " + newCell);

                    for (int i = 0; i < Board.NR_FOOD; i++) {
                        if (Board.food[i] != null &&
                                Board.food[i].getX() == newCell.getX() &&
                                Board.food[i].getY() == newCell.getY()) {

                            Board.gameboard[Board.food[i].getX()][Board.food[i].getY()].setFood(false);

                            while (x >= 0 && y >= 0 &&
                                    x < GridSize.GRID_HEIGHT &&
                                    y < GridSize.GRID_WIDTH &&
                                    Board.gameboard[x][y].hasElements()) {
                                x = random.nextInt(GridSize.GRID_HEIGHT);
                                y = random.nextInt(GridSize.GRID_WIDTH);
                            }

                            if (x >= 0 && y >= 0 &&
                                    x < GridSize.GRID_HEIGHT &&
                                    y < GridSize.GRID_WIDTH) {
                                Board.food[i] = new Cell(x, y);
                                Board.gameboard[x][y].setFood(true);
                            }
                        }
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[" + idt + "] Food check out of bounds at " + newCell);
        }
    }

    private Cell changeDirection(Cell currentHead) {
        if (currentHead == null) return null;

        try {
            Cell newCell = currentHead;

            // Avoid out of bounds
            while (direction == Direction.UP && (currentHead.getY() - 1) < 0) {
                if ((currentHead.getX() - 1) < 0) {
                    direction = Direction.RIGHT;
                } else if ((currentHead.getX() + 1) >= GridSize.GRID_WIDTH) {
                    direction = Direction.LEFT;
                } else {
                    randomMovement(newCell);
                }
            }

            while (direction == Direction.DOWN && (currentHead.getY() + 1) >= GridSize.GRID_HEIGHT) {
                if ((currentHead.getX() - 1) < 0) {
                    direction = Direction.RIGHT;
                } else if ((currentHead.getX() + 1) >= GridSize.GRID_WIDTH) {
                    direction = Direction.LEFT;
                } else {
                    randomMovement(newCell);
                }
            }

            while (direction == Direction.LEFT && (currentHead.getX() - 1) < 0) {
                if ((currentHead.getY() - 1) < 0) {
                    direction = Direction.DOWN;
                } else if ((currentHead.getY() + 1) >= GridSize.GRID_HEIGHT) {
                    direction = Direction.UP;
                } else {
                    randomMovement(newCell);
                }
            }

            while (direction == Direction.RIGHT && (currentHead.getX() + 1) >= GridSize.GRID_WIDTH) {
                if ((currentHead.getY() - 1) < 0) {
                    direction = Direction.DOWN;
                } else if ((currentHead.getY() + 1) >= GridSize.GRID_HEIGHT) {
                    direction = Direction.UP;
                } else {
                    randomMovement(newCell);
                }
            }

            switch (direction) {
                case Direction.UP:
                    if (currentHead.getY() - 1 >= 0) {
                        newCell = Board.gameboard[currentHead.getX()][currentHead.getY() - 1];
                    }
                    break;
                case Direction.DOWN:
                    if (currentHead.getY() + 1 < GridSize.GRID_HEIGHT) {
                        newCell = Board.gameboard[currentHead.getX()][currentHead.getY() + 1];
                    }
                    break;
                case Direction.LEFT:
                    if (currentHead.getX() - 1 >= 0) {
                        newCell = Board.gameboard[currentHead.getX() - 1][currentHead.getY()];
                    }
                    break;
                case Direction.RIGHT:
                    if (currentHead.getX() + 1 < GridSize.GRID_WIDTH) {
                        newCell = Board.gameboard[currentHead.getX() + 1][currentHead.getY()];
                    }
                    break;
            }

            return newCell;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("[" + idt + "] Direction change out of bounds at " + currentHead);
            return null;
        }
    }

    public synchronized LinkedList<Cell> getBody() {
        return new LinkedList<>(this.snakeBody);
    }

    public boolean isSelected() {
        return isSelected;
    }

    public synchronized void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getIdt() {
        return idt;
    }

    public synchronized void setSnakeEnd(boolean end) {
        this.snakeEnd = end;
    }

    public synchronized Cell getHead() {
        return snakeBody.peekFirst();
    }
}