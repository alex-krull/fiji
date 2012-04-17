package net.imglib2.algorithm.componenttree;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.PriorityQueue;

import net.imglib2.Localizable;
import net.imglib2.Location;
import net.imglib2.Positionable;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.componenttree.pixellist.PixelListComponentTree;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.logic.BitType;

/**
 * Build the component tree of an image. This is an implementation of the
 * algorithm described by D. Nister and H. Stewenius in
 * "Linear Time Maximally Stable Extremal Regions" (ECCV 2008).
 *
 * The input to the algorithm is a RandomAccessibleInterval< T >. Further, a
 * Comparator<T> and a {@link Component.Generator} to instantiate new components
 * are required. Pixel locations are aggregated in {@link Component}s which are
 * passed to a {@link Component.Handler} whenever a connected component for a
 * specific threshold is completed.
 *
 * Building up a tree structure out of the completed components should happen in
 * the {@link Component.Handler} implementation. See {@link PixelListComponentTree}
 * for an example.
 *
 * <p>
 * <strong>TODO</strong> Add support for non-zero-min RandomAccessibleIntervals.
 * (Currently, we assume that the input image is a <em>zero-min</em> interval.)
 * </p>
 *
 * @author Tobias Pietzsch
 *
 * @param <T>
 *            value type of the input image.
 * @param <C>
 *            component type.
 */
public final class ComponentTree< T extends Type< T >, C extends Component< T > >
{
	/**
	 * Run the algorithm. Completed components are emitted to the
	 * {@link Component.Handler} which is responsible for building up the
	 * tree structure. An implementations of {@link Component.Handler} is
	 * provided for example by {@link PixelListComponentTree}.
	 *
	 * @param input
	 *            input image.
	 * @param componentGenerator
	 *            provides new {@link Component} instances.
	 * @param componentOutput
	 *            receives completed {@link Component}s.
	 * @param comparator
	 *            determines ordering of threshold values.
	 */
	public static < T extends Type< T >, C extends Component< T > > void buildComponentTree( final RandomAccessibleInterval< T > input, final Component.Generator< T, C > componentGenerator, final Component.Handler< C > componentHandler, final Comparator< T > comparator )
	{
		new ComponentTree< T, C >( input, componentGenerator, componentHandler, comparator );
	}

	/**
	 * Run the algorithm. Completed components are emitted to the
	 * {@link Component.Handler} which is responsible for building up the tree
	 * structure. An implementations of {@link Component.Handler} is provided
	 * for example by {@link PixelListComponentTree}.
	 *
	 * @param input
	 *            input image of a comparable value type.
	 * @param componentGenerator
	 *            provides new {@link Component} instances.
	 * @param componentOutput
	 *            receives completed {@link Component}s.
	 * @param darkToBright
	 *            determines ordering of threshold values. If it is true, then
	 *            thresholds are applied from low to high values. Note that the
	 *            {@link Component.Generator#createMaxComponent()} needs to
	 *            match this ordering. For example when IntType using
	 *            darkToBright=false, then
	 *            {@link Component.Generator#createMaxComponent()} should
	 *            provide a Integer.MIN_VALUE valued component.
	 */
	public static < T extends Type< T > & Comparable< T >, C extends Component< T > > void buildComponentTree( final RandomAccessibleInterval< T > input, final Component.Generator< T, C > componentGenerator, final Component.Handler< C > componentHandler, boolean darkToBright )
	{
		new ComponentTree< T, C >( input, componentGenerator, componentHandler, darkToBright ? new DarkToBright< T >() : new BrightToDark< T >() );
	}

	/**
	 * Default comparator for {@link Comparable} pixel values for dark-to-bright pass.
	 */
	public static final class DarkToBright< T extends Comparable< T > > implements Comparator< T >
	{
		@Override
		public int compare( final T o1, final T o2 )
		{
			return o1.compareTo( o2 );
		}
	}

	/**
	 * Default comparator for {@link Comparable} pixel values for bright-to-dark pass.
	 */
	public static final class BrightToDark< T extends Comparable< T > > implements Comparator< T >
	{
		@Override
		public int compare( final T o1, final T o2 )
		{
			return o2.compareTo( o1 );
		}
	}

	/**
	 * Iterate pixel positions in 4-neighborhood.
	 */
	private static final class Neighborhood
	{
		/**
		 * index of the next neighbor to visit. 0 is pixel at x-1, 1 is pixel at
		 * x+1, 2 is pixel at y-1, 3 is pixel at y+1, and so on.
		 */
		private int n;

		/**
		 * number of neighbors, e.g., 4 for 2d images.
		 */
		private final int nBound;

		/**
		 * image dimensions. used to check out-of-bounds.
		 */
		final long[] dimensions;

		public Neighborhood( final long[] dim )
		{
			n = 0;
			nBound = dim.length * 2;
			dimensions = dim;
		}

		public int getNextNeighborIndex()
		{
			return n;
		}

		public void setNextNeighborIndex( int n )
		{
			this.n = n;
		}

		public void reset()
		{
			n = 0;
		}

		public boolean hasNext()
		{
			return n < nBound;
		}

		/**
		 * Set neighbor to the next (according to
		 * {@link ComponentTree.Neighborhood#n}) neighbor position of current.
		 *
		 * @param current
		 * @param neighbor
		 * @return false if the neighbor position is out of bounds, true
		 *         otherwise.
		 */
		public boolean next( final Localizable current, final Positionable neighbor )
		{
			// TODO: can setting full position be avoided?
			neighbor.setPosition( current );
			final int d = n / 2;
			if ( n % 2 == 0 )
			{
				neighbor.move( -1, d );
				++n;
				return current.getLongPosition( d ) - 1 >= 0;
			}
			else
			{
				neighbor.move( 1, d );
				++n;
				return current.getLongPosition( d ) + 1 < dimensions[ d ];
			}
		}
	}

	/**
	 * A pixel position on the heap of boundary pixels to be processed next. The
	 * heap is sorted by pixel values.
	 */
	private final class BoundaryPixel extends Location implements Comparable< BoundaryPixel >
	{
		private final T value;

		// TODO: this should be some kind of iterator over the neighborhood
		private final int nextNeighborIndex;

		public BoundaryPixel( final Localizable position, final T value, int nextNeighborIndex )
		{
			super( position );
			this.nextNeighborIndex = nextNeighborIndex;
			this.value = value.copy();
		}

		public int getNextNeighborIndex()
		{
			return nextNeighborIndex;
		}

		public T get()
		{
			return value;
		}

		@Override
		public int compareTo( BoundaryPixel o )
		{
			return comparator.compare( value, o.value );
		}
	}

	private final Component.Generator< T, C > componentGenerator;

	private final Component.Handler< C > componentOutput;

	private final Neighborhood neighborhood;

	private final RandomAccessible< BitType > visited;

	private final RandomAccess< BitType > visitedRandomAccess;

	private final PriorityQueue< BoundaryPixel > boundaryPixels;

	private final Deque< C > componentStack;

	private final Comparator< T > comparator;

	/**
	 * Set up data structures and run the algorithm. Completed components are
	 * emitted to the provided {@link Component.Handler}.
	 *
	 * @param input
	 *            input image.
	 * @param componentGenerator
	 *            provides new {@link Component} instances.
	 * @param componentOutput
	 *            receives completed {@link Component}s.
	 * @param comparator
	 *            determines ordering of threshold values.
	 */
	private ComponentTree( final RandomAccessibleInterval< T > input, final Component.Generator< T, C > componentGenerator, final Component.Handler< C > componentOutput, final Comparator< T > comparator )
	{
		this.componentGenerator = componentGenerator;
		this.componentOutput = componentOutput;

		final long[] dimensions = new long[ input.numDimensions() ];
		input.dimensions( dimensions );

		ImgFactory< BitType > imgFactory = new ArrayImgFactory< BitType >();
		visited = imgFactory.create( dimensions, new BitType() );
		visitedRandomAccess = visited.randomAccess();

		neighborhood = new Neighborhood( dimensions );

		boundaryPixels = new PriorityQueue< BoundaryPixel >();

		componentStack = new ArrayDeque< C >();
		componentStack.push( componentGenerator.createMaxComponent() );

		this.comparator = comparator;

		run( input );
	}

	/**
	 * Mark the given pixel location as visited.
	 */
	private void visit( final Localizable position )
	{
		visitedRandomAccess.setPosition( position );
		visitedRandomAccess.get().set( true );
	}

	/**
	 * Was the given pixel location already visited?
	 */
	private boolean wasVisited( final Localizable position )
	{
		visitedRandomAccess.setPosition( position );
		return visitedRandomAccess.get().get();
	}

	/**
	 * Main loop of the algorithm. This follows exactly along steps of the
	 * algorithm as described in the paper.
	 *
	 * @param input
	 *            the input image.
	 */
	private void run( final RandomAccessibleInterval< T > input )
	{
		RandomAccess< T > current = input.randomAccess();
		RandomAccess< T > neighbor = input.randomAccess();
		input.min( current );
		T currentLevel = current.get().createVariable();
		T neighborLevel = current.get().createVariable();

		// Note that step numbers in the comments below refer to steps in the
		// Nister & Stewenius paper.

		// step 2
		visit( current );
		currentLevel.set( current.get() );

		// step 3
		componentStack.push( componentGenerator.createComponent( currentLevel ) );

		// step 4
		while ( true )
		{
			while ( neighborhood.hasNext() )
			{
				if ( !neighborhood.next( current, neighbor ) )
					continue;
				if ( !wasVisited( neighbor ) )
				{
					visit( neighbor );
					neighborLevel.set( neighbor.get() );
					if ( comparator.compare( neighborLevel, currentLevel ) >= 0 )
					{
						boundaryPixels.add( new BoundaryPixel( neighbor, neighborLevel, 0 ) );
					}
					else
					{
						boundaryPixels.add( new BoundaryPixel( current, currentLevel, neighborhood.getNextNeighborIndex() ) );
						current.setPosition( neighbor );
						currentLevel.set( neighborLevel );

						// go to 3, i.e.:
						componentStack.push( componentGenerator.createComponent( currentLevel ) );
						neighborhood.reset();
					}
				}
			}

			// step 5
			C component = componentStack.peek();
			component.addPosition( current );

			// step 6
			if ( boundaryPixels.isEmpty() )
			{
				processStack( currentLevel );
				return;
			}

			BoundaryPixel p = boundaryPixels.poll();
			if ( comparator.compare( p.get(), currentLevel ) != 0 )
			{
				// step 7
				processStack( p.get() );
			}
			current.setPosition( p );
			currentLevel.set( p.get() );
			neighborhood.setNextNeighborIndex( p.getNextNeighborIndex() );
		}
	}

	/**
	 * This is called whenever the current value is raised.
	 *
	 * @param value
	 */
	private void processStack( T value )
	{
		while ( true )
		{
			// process component on top of stack
			C component = componentStack.pop();
			componentOutput.emit( component );

			// get level of second component on stack
			C secondComponent = componentStack.peek();
			try
			{
				final int c = comparator.compare( value, secondComponent.getValue() );
				if ( c < 0 )
				{
					component.setValue( value );
					componentStack.push( component );
				}
				else
				{
					secondComponent.merge( component );
					if ( c > 0 )
						continue;
				}
				return;
			}
			catch ( NullPointerException e )
			{
				componentStack.push( component );
				return;
			}
		}
	}
}
