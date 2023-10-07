package chess;

import java.util.ArrayList;
import java.util.List;

import boardgame.Board;
import boardgame.Piece;
import boardgame.Position;
import chess.pieces.King;
import chess.pieces.Rook;

public class ChessMatch {

	private int turn;
	private Color currentPlayer;
	private Board board;

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
		NextTurn();
		return (ChessPiece) capturedPiece;
	}

	private Piece MakeMove(Position source, Position target) {
		Piece p = board.RemovePiece(source);
		Piece capturedPiece = board.RemovePiece(target);
		board.PlacePiece(p, target);
		if (capturedPiece != null) {
			piecesOnTheBoard.remove(capturedPiece);
			capturedPieces.add(capturedPiece);
		}
		return capturedPiece;
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

	private void placeNewPiece(char column, int row, ChessPiece piece) {
		board.PlacePiece(piece, new ChessPosition(column, row).toPosition());
		piecesOnTheBoard.add(piece);
	}

	private void InitialSetup() {
		placeNewPiece('c', 1, new Rook(board, Color.RED));
		placeNewPiece('c', 2, new Rook(board, Color.RED));
		placeNewPiece('d', 2, new Rook(board, Color.RED));
		placeNewPiece('e', 2, new Rook(board, Color.RED));
		placeNewPiece('e', 1, new Rook(board, Color.RED));
		placeNewPiece('d', 1, new King(board, Color.RED));

		placeNewPiece('c', 7, new Rook(board, Color.YELLOW));
		placeNewPiece('c', 8, new Rook(board, Color.YELLOW));
		placeNewPiece('d', 7, new Rook(board, Color.YELLOW));
		placeNewPiece('e', 7, new Rook(board, Color.YELLOW));
		placeNewPiece('e', 8, new Rook(board, Color.YELLOW));
		placeNewPiece('d', 8, new King(board, Color.YELLOW));
	}
}
