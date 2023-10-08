package chess;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.Bishop;
import chess.pieces.King;
import chess.pieces.Knight;
import chess.pieces.Pawn;
import chess.pieces.Rook;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;
	private boolean check;
	private boolean checkMate;

	private List<Piece> piecesOnTheBoard = new ArrayList<>();
	private List<Piece> capturedPieces = new ArrayList<>();

	public ChessMatch() {
		board = new Board(8, 8);
		turn = 1;
		currentPlayer = Color.RED;
		InitialSetup();
	}

	public int getTurn() {
		return turn;
	}

	public Color getCurrentPlayer() {
		return currentPlayer;
	}

	public boolean getCheck() {
		return check;
	}

	public boolean getCheckMate() {
		return checkMate;
	}

	public ChessPiece[][] getPieces() {
		ChessPiece[][] mat = new ChessPiece[board.getRows()][board.getColumns()];
		for (int i = 0; i < board.getRows(); i++) {
			for (int j = 0; j < board.getColumns(); j++) {
				mat[i][j] = (ChessPiece) board.piece(i, j);
			}
		}
		return mat;
	}

	public boolean[][] possibleMoves(ChessPosition sourcePosition) {
		Position position = sourcePosition.toPosition();
		ValidadeSourcePosition(position);
		return board.piece(position).possibleMoves();
	}

	public ChessPiece performChessMove(ChessPosition sourcePosition, ChessPosition targetPosition) {
		Position source = sourcePosition.toPosition();
		Position target = targetPosition.toPosition();
		ValidadeSourcePosition(source);
		ValidateTargetPosition(source, target);

		Piece capturedPiece = MakeMove(source, target);
		if (TestCheck(currentPlayer)) {
			UndoMove(source, target, capturedPiece);
			throw new ChessException("Vc nao pode se colocar em check");
		}

		check = (TestCheck(opponent(currentPlayer))) ? true : false;

		// testar se o jogo acabou
		if (TestCheckmate(opponent(currentPlayer))) {
			checkMate = true;
		} else {
			NextTurn();
		}

		return (ChessPiece) capturedPiece;
	}

	private Piece MakeMove(Position source, Position target) {

		ChessPiece p = (ChessPiece) board.RemovePiece(source);
		p.increaseMoveCount();

		Piece capturedPiece = board.RemovePiece(target);
		board.PlacePiece(p, target);

		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		return capturedPiece;
	}

	private void UndoMove(Position source, Position target, Piece capturedPiece) {

		ChessPiece p = (ChessPiece) board.RemovePiece(target);
		p.decreaseMoveCount();
		board.PlacePiece(p, source);

		// desfazendo a jogada
		if (capturedPiece != null) {
			board.PlacePiece(capturedPiece, target);
			capturedPieces.remove(capturedPiece);
			piecesOnTheBoard.add(capturedPiece);
		}
	}

	private void ValidadeSourcePosition(Position position) {
		if (!board.thereIsAPiece(position)) {
			throw new ChessException("Nao existe peca na posicao de origim");
		}
		if (currentPlayer != ((ChessPiece) board.piece(position)).getColor()) {
			throw new ChessException("A peca escolhida nao e sua");
		}
		if (!board.piece(position).IsThereAnyPossibleMove()) {
			throw new ChessException("Nao existe movimentos possiveis para a peca escolhida");
		}
	}

	private void ValidateTargetPosition(Position source, Position target) {
		if (!board.piece(source).possibleMove(target)) {
			throw new ChessException("A peca escolhida nao pode se mover para a posicao de destino");
		}
	}

	private void NextTurn() {
		turn++;
		currentPlayer = (currentPlayer == Color.RED) ? Color.YELLOW : Color.RED;
	}

	private Color opponent(Color color) {
		return (color == Color.RED) ? Color.YELLOW : Color.RED;
	}

	private ChessPiece king(Color color) {
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());

		for (Piece p : list) {
			if (p instanceof King) {
				return (ChessPiece) p;
			}
		}
		throw new IllegalStateException("Nao existe " + color + "Rei no tabuleiro");
	}

	private boolean TestCheck(Color color) {
		Position kingPosition = king(color).getChessPosition().toPosition();
		List<Piece> opponentPieces = piecesOnTheBoard.stream()
				.filter(x -> ((ChessPiece) x).getColor() == opponent(color)).collect(Collectors.toList());

		for (Piece p : opponentPieces) {
			boolean[][] mat = p.possibleMoves();
			if (mat[kingPosition.getRow()][kingPosition.getColumn()]) {
				return true;
			}
		}
		return false;
	}

	private boolean TestCheckmate(Color color) {
		if (!TestCheck(color)) {
			return false;
		}
		List<Piece> list = piecesOnTheBoard.stream().filter(x -> ((ChessPiece) x).getColor() == color)
				.collect(Collectors.toList());

		for (Piece p : list) {

			boolean[][] mat = p.possibleMoves();
			for (int i = 0; i < board.getRows(); i++) {
				for (int j = 0; j < board.getColumns(); j++) {
					if (mat[i][j]) {
						Position source = ((ChessPiece) p).getChessPosition().toPosition();
						Position target = new Position(i, j);
						Piece capturedPiece = MakeMove(source, target);

						boolean testCheck = TestCheck(color);
						UndoMove(source, target, capturedPiece);
						if (!testCheck) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.PlacePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}

	private void InitialSetup() {
		placeNewPiece('a', 1, new Rook(board, Color.RED));
		placeNewPiece('b', 1, new Knight(board, Color.RED));
		placeNewPiece('c', 1, new Bishop(board, Color.RED));
		placeNewPiece('e', 1, new King(board, Color.RED));
		placeNewPiece('f', 1, new Bishop(board, Color.RED));
		placeNewPiece('g', 1, new Knight(board, Color.RED));
		placeNewPiece('h', 1, new Rook(board, Color.RED));
		placeNewPiece('a', 2, new Pawn(board, Color.RED));
		placeNewPiece('b', 2, new Pawn(board, Color.RED));
		placeNewPiece('c', 2, new Pawn(board, Color.RED));
		placeNewPiece('d', 2, new Pawn(board, Color.RED));
		placeNewPiece('e', 2, new Pawn(board, Color.RED));
		placeNewPiece('f', 2, new Pawn(board, Color.RED));
		placeNewPiece('g', 2, new Pawn(board, Color.RED));
		placeNewPiece('h', 2, new Pawn(board, Color.RED));

		placeNewPiece('a', 8, new Rook(board, Color.YELLOW));
		placeNewPiece('b', 8, new Knight(board, Color.YELLOW));
		placeNewPiece('c', 8, new Bishop(board, Color.YELLOW));
		placeNewPiece('e', 8, new King(board, Color.YELLOW));
		placeNewPiece('f', 8, new Bishop(board, Color.YELLOW));
		placeNewPiece('g', 8, new Knight(board, Color.YELLOW));
		placeNewPiece('h', 8, new Rook(board, Color.YELLOW));
		placeNewPiece('a', 7, new Pawn(board, Color.YELLOW));
		placeNewPiece('b', 7, new Pawn(board, Color.YELLOW));
		placeNewPiece('c', 7, new Pawn(board, Color.YELLOW));
		placeNewPiece('d', 7, new Pawn(board, Color.YELLOW));
		placeNewPiece('e', 7, new Pawn(board, Color.YELLOW));
		placeNewPiece('f', 7, new Pawn(board, Color.YELLOW));
		placeNewPiece('g', 7, new Pawn(board, Color.YELLOW));
		placeNewPiece('h', 7, new Pawn(board, Color.YELLOW));

	}
}
